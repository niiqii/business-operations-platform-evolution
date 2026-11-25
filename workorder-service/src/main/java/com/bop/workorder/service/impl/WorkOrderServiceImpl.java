package com.bop.workorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bop.workorder.client.LanderClient;
import com.bop.workorder.client.NotifyClient;
import com.bop.workorder.client.dto.LanderCallbackPayload;
import com.bop.workorder.client.dto.LanderCompleteTaskRequest;
import com.bop.workorder.client.dto.LanderStartProcessRequest;
import com.bop.workorder.client.dto.LanderTaskObject;
import com.bop.workorder.client.dto.LanderUserInfo;
import com.bop.workorder.common.BizException;
import com.bop.workorder.common.PageResult;
import com.bop.workorder.common.UserContext;
import com.bop.workorder.dto.WorkOrderApproveRequest;
import com.bop.workorder.dto.WorkOrderCloseRequest;
import com.bop.workorder.dto.WorkOrderCreateRequest;
import com.bop.workorder.dto.WorkOrderDetailVO;
import com.bop.workorder.dto.WorkOrderQueryRequest;
import com.bop.workorder.dto.WorkOrderSubmitRequest;
import com.bop.workorder.entity.WorkOrder;
import com.bop.workorder.entity.WorkOrderLog;
import com.bop.workorder.enums.BizType;
import com.bop.workorder.enums.WorkOrderStatus;
import com.bop.workorder.mapper.WorkOrderLogMapper;
import com.bop.workorder.mapper.WorkOrderMapper;
import com.bop.workorder.service.FormSchemaService;
import com.bop.workorder.service.WorkOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderServiceImpl.class);
    private static final DateTimeFormatter WO_NO_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int CALLBACK_LOOKUP_RETRIES = 6;
    private static final long CALLBACK_LOOKUP_INTERVAL_MS = 100L;

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final FormSchemaService formSchemaService;
    private final LanderClient landerClient;
    private final NotifyClient notifyClient;

    public WorkOrderServiceImpl(
            WorkOrderMapper workOrderMapper,
            WorkOrderLogMapper workOrderLogMapper,
            FormSchemaService formSchemaService,
            LanderClient landerClient,
            NotifyClient notifyClient) {
        this.workOrderMapper = workOrderMapper;
        this.workOrderLogMapper = workOrderLogMapper;
        this.formSchemaService = formSchemaService;
        this.landerClient = landerClient;
        this.notifyClient = notifyClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder createWorkOrder(WorkOrderCreateRequest request) {
        BizType bizType = BizType.fromCode(request.getBizType());
        if (bizType == null) {
            throw new BizException(20002, "Unsupported bizType: " + request.getBizType());
        }
        int schemaVersion = request.getSchemaVersion() != null ? request.getSchemaVersion() : 1;
        formSchemaService.requireTemplate(bizType.name(), schemaVersion);

        String operator = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();

        WorkOrder order = new WorkOrder();
        order.setWoNo(generateWoNo(bizType));
        order.setOrderNo(request.getOrderNo());
        order.setActivityNo(request.getActivityNo());
        order.setVendorId(request.getVendorId());
        order.setCityCode(request.getCityCode());
        order.setSettlePeriod(request.getSettlePeriod());
        order.setBizType(bizType.name());
        order.setSchemaVersion(schemaVersion);
        order.setStatus(WorkOrderStatus.VENDOR_FILLING.name());
        order.setCurrentHandler(request.getVendorHandler());
        order.setCalcAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        order.setExtJson(null);
        order.setSlaDeadline(request.getSlaDeadline() != null ? request.getSlaDeadline() : now.plusDays(7));
        order.setVersion(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        workOrderMapper.insert(order);
        saveLog(order.getWoNo(), WorkOrderStatus.DRAFT.name(), WorkOrderStatus.VENDOR_FILLING.name(),
                operator, "DISPATCH", request.getRemark());

        String dispatchStr = landerClient.buildDispatchString(request.getVendorHandler(), request.getVendorHandlerName());
        LanderStartProcessRequest startReq = new LanderStartProcessRequest();
        startReq.setBaseSchema(bizType.getBaseSchema());
        startReq.setApplyId(order.getWoNo());
        startReq.setTitle(order.getSettlePeriod() + "-" + bizType.name() + "-" + order.getWoNo());
        startReq.setSubmit(Boolean.TRUE);
        startReq.setHandler(new LanderUserInfo(operator, operator, "DEPT-01", "运营部门", "建单人"));
        startReq.setNextHandlers(landerClient.parseDispatchString(dispatchStr));
        Map<String, String> extendData = new HashMap<String, String>();
        extendData.put("actionCode", "BtnDispatch");
        extendData.put("assigner", dispatchStr);
        extendData.put("process_mark", "0");
        startReq.setExtendData(extendData);

        String piid = landerClient.startProcess(startReq);
        order.setLanderPiid(piid);
        order.setUpdateTime(LocalDateTime.now());
        workOrderMapper.updateById(order);

        notifyClient.send(
                request.getVendorHandler(),
                "工单派发通知",
                "您有新的" + bizType.name() + "工单待填报：" + order.getWoNo(),
                order.getWoNo(),
                "WO_DISPATCH"
        );
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder submitWorkOrder(String woNo, WorkOrderSubmitRequest request) {
        WorkOrder order = requireByWoNo(woNo);
        String currentStatus = order.getStatus();
        if (!WorkOrderStatus.VENDOR_FILLING.name().equals(currentStatus)
                && !WorkOrderStatus.REJECTED.name().equals(currentStatus)) {
            throw new BizException(20004, "Current status does not allow submit: " + currentStatus);
        }
        if (!StringUtils.hasText(order.getLanderTiid())) {
            throw new BizException(20004, "Work order has no active Lander task id");
        }

        BizType bizType = BizType.fromCode(order.getBizType());
        BigDecimal calcAmount = formSchemaService.validateAndCalculate(
                bizType, order.getSchemaVersion(), request.getExtJson());

        String operator = UserContext.getUserId();
        order.setExtJson(request.getExtJson());
        order.setCalcAmount(calcAmount);
        order.setStatus(WorkOrderStatus.PM_APPROVING.name());
        order.setCurrentHandler(request.getApproverId());
        order.setUpdateTime(LocalDateTime.now());

        int updated = workOrderMapper.updateById(order);
        if (updated == 0) {
            throw new BizException(20004, "Concurrent modification detected for work order: " + woNo);
        }
        saveLog(woNo, currentStatus, WorkOrderStatus.PM_APPROVING.name(), operator, "SUBMIT", request.getRemark());

        String dispatchStr = landerClient.buildDispatchString(request.getApproverId(), request.getApproverName());
        LanderCompleteTaskRequest completeReq = new LanderCompleteTaskRequest();
        completeReq.setBaseSchema(bizType.getBaseSchema());
        completeReq.setTiid(order.getLanderTiid());
        completeReq.setHandler(new LanderUserInfo(operator, operator, "DEPT-01", "服务商", "建单人"));
        if (WorkOrderStatus.REJECTED.name().equals(currentStatus)) {
            // 驳回后流程停在 _BOF，重提需显式跳回经理审批节点 step02
            completeReq.setDecision("JUMP");
            completeReq.setNextNode("step02");
        } else {
            completeReq.setDecision("SUBMIT");
        }
        completeReq.setOption("送项目经理审批");
        completeReq.setNotion(StringUtils.hasText(request.getRemark()) ? request.getRemark() : "已填报提交");
        completeReq.setNextHandlers(landerClient.parseDispatchString(dispatchStr));
        Map<String, String> extendData = new HashMap<String, String>();
        extendData.put("actionCode", "BtnSubmit");
        extendData.put("assigner", dispatchStr);
        extendData.put("task_mark", "0");
        completeReq.setExtendData(extendData);

        landerClient.completeTask(completeReq);

        notifyClient.send(
                request.getApproverId(),
                "工单待审批通知",
                "工单 " + woNo + " 已提交，核算金额 " + calcAmount.toPlainString() + "，请及时审批",
                woNo,
                "WO_SUBMIT"
        );
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder approveWorkOrder(String woNo, WorkOrderApproveRequest request) {
        WorkOrder order = requireByWoNo(woNo);
        if (!WorkOrderStatus.PM_APPROVING.name().equals(order.getStatus())) {
            throw new BizException(20004, "Current status does not allow approval: " + order.getStatus());
        }
        if (!StringUtils.hasText(order.getLanderTiid())) {
            throw new BizException(20004, "Work order has no active Lander task id");
        }

        String decision = request.getDecision().trim().toUpperCase();
        if (!"YES".equals(decision) && !"REJECT".equals(decision)) {
            throw new BizException(20002, "decision must be YES or REJECT");
        }

        String operator = UserContext.getUserId();
        String fromStatus = order.getStatus();
        String toStatus;
        String nextHandler;
        String notifyRecipient;
        String notifyContent;

        if ("YES".equals(decision)) {
            toStatus = WorkOrderStatus.CLOSED.name();
            nextHandler = null;
            notifyRecipient = order.getVendorId();
            notifyContent = "工单 " + woNo + " 已审批通过并归档";
        } else {
            toStatus = WorkOrderStatus.REJECTED.name();
            nextHandler = StringUtils.hasText(request.getNextHandler()) ? request.getNextHandler() : order.getVendorId();
            notifyRecipient = nextHandler;
            notifyContent = "工单 " + woNo + " 已被驳回，请修改后重新提交";
        }

        order.setStatus(toStatus);
        order.setCurrentHandler(nextHandler);
        order.setUpdateTime(LocalDateTime.now());
        int updated = workOrderMapper.updateById(order);
        if (updated == 0) {
            throw new BizException(20004, "Concurrent modification detected for work order: " + woNo);
        }
        saveLog(woNo, fromStatus, toStatus, operator, "YES".equals(decision) ? "APPROVE" : "REJECT", request.getRemark());

        BizType bizType = BizType.fromCode(order.getBizType());
        LanderCompleteTaskRequest completeReq = new LanderCompleteTaskRequest();
        completeReq.setBaseSchema(bizType.getBaseSchema());
        completeReq.setTiid(order.getLanderTiid());
        completeReq.setHandler(new LanderUserInfo(operator, operator, "DEPT-01", "项目经理", "建单人"));
        completeReq.setDecision(decision);
        completeReq.setOption("YES".equals(decision) ? "审批通过归档" : "驳回修改");
        completeReq.setNotion(StringUtils.hasText(request.getRemark()) ? request.getRemark() : decision);

        Map<String, String> extendData = new HashMap<String, String>();
        extendData.put("actionCode", "YES".equals(decision) ? "BtnApprove" : "BtnReject");
        extendData.put("task_mark", "0");
        if (StringUtils.hasText(nextHandler)) {
            String dispatchStr = landerClient.buildDispatchString(nextHandler, request.getNextHandlerName());
            completeReq.setNextHandlers(landerClient.parseDispatchString(dispatchStr));
            extendData.put("assigner", dispatchStr);
        }
        completeReq.setExtendData(extendData);

        landerClient.completeTask(completeReq);
        notifyClient.send(notifyRecipient, "工单审批结果通知", notifyContent, woNo, "WO_APPROVE");
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkOrder closeWorkOrder(String woNo, WorkOrderCloseRequest request) {
        WorkOrder order = requireByWoNo(woNo);
        if (WorkOrderStatus.CLOSED.name().equals(order.getStatus())) {
            return order;
        }
        String fromStatus = order.getStatus();
        order.setStatus(WorkOrderStatus.CLOSED.name());
        order.setCurrentHandler(null);
        order.setUpdateTime(LocalDateTime.now());
        int updated = workOrderMapper.updateById(order);
        if (updated == 0) {
            throw new BizException(20004, "Concurrent modification detected for work order: " + woNo);
        }

        String reason = request != null ? request.getReason() : null;
        saveLog(woNo, fromStatus, WorkOrderStatus.CLOSED.name(), UserContext.getUserId(), "CLOSE", reason);
        return order;
    }

    @Override
    public WorkOrderDetailVO getDetail(String woNo) {
        WorkOrder order = requireByWoNo(woNo);
        List<WorkOrderLog> logs = workOrderLogMapper.selectList(new LambdaQueryWrapper<WorkOrderLog>()
                .eq(WorkOrderLog::getWoNo, woNo)
                .orderByAsc(WorkOrderLog::getId));
        return new WorkOrderDetailVO(order, logs);
    }

    @Override
    public PageResult<WorkOrder> queryPage(WorkOrderQueryRequest request) {
        long pageNo = request.getPageNo() > 0 ? request.getPageNo() : 1;
        long pageSize = request.getPageSize() > 0 ? Math.min(request.getPageSize(), 200) : 20;

        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(StringUtils.hasText(request.getCityCode()), WorkOrder::getCityCode, request.getCityCode())
                .eq(StringUtils.hasText(request.getStatus()), WorkOrder::getStatus, request.getStatus())
                .eq(StringUtils.hasText(request.getVendorId()), WorkOrder::getVendorId, request.getVendorId())
                .eq(StringUtils.hasText(request.getSettlePeriod()), WorkOrder::getSettlePeriod, request.getSettlePeriod())
                .orderByDesc(WorkOrder::getId);

        Page<WorkOrder> page = workOrderMapper.selectPage(new Page<WorkOrder>(pageNo, pageSize), wrapper);
        return new PageResult<WorkOrder>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    public void handleLanderCallback(LanderCallbackPayload payload) {
        if (payload == null) {
            return;
        }
        WorkOrder order = findOrderForCallback(payload.getApplyId(), payload.getPiid());
        if (order == null) {
            log.warn("Callback target work order not found, applyId={}, piid={}", payload.getApplyId(), payload.getPiid());
            return;
        }

        String nodeId = null;
        String activeTiid = order.getLanderTiid();
        String nextHandler = order.getCurrentHandler();

        if (!CollectionUtils.isEmpty(payload.getCallbackInfos())) {
            LanderCallbackPayload.CallbackInfo info = payload.getCallbackInfos().get(0);
            nodeId = info.getNodeId();
            if (!CollectionUtils.isEmpty(info.getCallbackTasks())) {
                LanderCallbackPayload.CallbackTask task = info.getCallbackTasks().get(0);
                activeTiid = task.getTiid();
                if (task.getOriginalOwner() != null) {
                    nextHandler = landerClient.extractAccount(task.getOriginalOwner().getUserId());
                }
                if (StringUtils.hasText(activeTiid)) {
                    LanderTaskObject remoteTask = landerClient.getTask(activeTiid);
                    if (!payload.isEndFlow() && remoteTask != null && StringUtils.hasText(remoteTask.getNodeId())) {
                        nodeId = remoteTask.getNodeId();
                    }
                }
            } else if (!CollectionUtils.isEmpty(info.getPreCallbackTasks())) {
                String completedTiid = info.getPreCallbackTasks().get(0).getTiid();
                if (StringUtils.hasText(completedTiid)) {
                    landerClient.getTask(completedTiid);
                }
            }
        }

        String targetStatus = resolveStatusFromCallback(payload.isEndFlow(), nodeId, order.getStatus());
        if (WorkOrderStatus.CLOSED.name().equals(targetStatus)) {
            nextHandler = null;
        }

        String fromStatus = order.getStatus();
        workOrderMapper.update(null, new LambdaUpdateWrapper<WorkOrder>()
                .eq(WorkOrder::getId, order.getId())
                .set(WorkOrder::getStatus, targetStatus)
                .set(StringUtils.hasText(payload.getPiid()), WorkOrder::getLanderPiid, payload.getPiid())
                .set(WorkOrder::getLanderTiid, activeTiid)
                .set(WorkOrder::getCurrentHandler, nextHandler)
                .set(WorkOrder::getUpdateTime, LocalDateTime.now()));

        String remark = "nodeId=" + nodeId + ", modifiedTime=" + payload.getModifiedTime() + ", endFlow=" + payload.isEndFlow();
        saveLog(order.getWoNo(), fromStatus, targetStatus, UserContext.getUserId(), "LANDER_CALLBACK", remark);
    }

    private WorkOrder findOrderForCallback(String applyId, String piid) {
        // 派单后异步回调可能早于主事务提交，短暂重试避免漏写 lander_tiid
        for (int i = 0; i < CALLBACK_LOOKUP_RETRIES; i++) {
            WorkOrder found = null;
            if (StringUtils.hasText(applyId)) {
                found = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getWoNo, applyId));
            }
            if (found == null && StringUtils.hasText(piid)) {
                found = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getLanderPiid, piid));
            }
            if (found != null) {
                return found;
            }
            if (i < CALLBACK_LOOKUP_RETRIES - 1) {
                try {
                    Thread.sleep(CALLBACK_LOOKUP_INTERVAL_MS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return null;
    }

    private String resolveStatusFromCallback(boolean endFlow, String nodeId, String fallbackStatus) {
        if (endFlow || "_EOF".equals(nodeId)) {
            return WorkOrderStatus.CLOSED.name();
        }
        if ("_BOF".equals(nodeId)) {
            return WorkOrderStatus.REJECTED.name();
        }
        if ("step02".equals(nodeId)) {
            return WorkOrderStatus.PM_APPROVING.name();
        }
        if ("step01".equals(nodeId)) {
            return WorkOrderStatus.VENDOR_FILLING.name();
        }
        return fallbackStatus;
    }

    private WorkOrder requireByWoNo(String woNo) {
        WorkOrder order = workOrderMapper.selectOne(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getWoNo, woNo));
        if (order == null) {
            throw new BizException(1006, "Work order not found: " + woNo);
        }
        return order;
    }

    private void saveLog(String woNo, String fromStatus, String toStatus, String operator, String action, String remark) {
        WorkOrderLog logEntry = new WorkOrderLog();
        logEntry.setWoNo(woNo);
        logEntry.setFromStatus(fromStatus);
        logEntry.setToStatus(toStatus);
        logEntry.setOperator(operator);
        logEntry.setAction(action);
        logEntry.setRemark(remark);
        logEntry.setCreateTime(LocalDateTime.now());
        workOrderLogMapper.insert(logEntry);
    }

    private String generateWoNo(BizType bizType) {
        String prefix = bizType == BizType.WORKHOUR ? "WH" : "AS";
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + LocalDateTime.now().format(WO_NO_TIME_FMT) + suffix;
    }
}
