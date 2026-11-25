package com.bop.mock.lander.service;

import com.bop.mock.chaos.ChaosConfig;
import com.bop.mock.chaos.ChaosConfigService;
import com.bop.mock.common.BizException;
import com.bop.mock.common.ChaosFaultException;
import com.bop.mock.lander.model.CallbackLogEntry;
import com.bop.mock.lander.model.CallbackPayload;
import com.bop.mock.lander.model.CompleteTaskRequest;
import com.bop.mock.lander.model.LanderUserInfo;
import com.bop.mock.lander.model.MockProcessInstance;
import com.bop.mock.lander.model.StartProcessRequest;
import com.bop.mock.lander.model.TaskObject;
import com.bop.mock.lander.model.TerminateProcessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LanderMockService {

    private static final int PROCESS_STATE_RUN = 1;
    private static final int PROCESS_STATE_END = 2;
    private static final int PROCESS_STATE_STOP = 3;

    private static final int TASK_STATE_READY = 1;
    private static final int TASK_STATE_COMPLETED = 2;
    private static final int TASK_STATE_TERMINATED = 4;

    private static final Set<String> VALID_DECISIONS = Set.of("YES", "SUBMIT", "REJECT", "JUMP", "CANCEL");

    private final ChaosConfigService chaosConfigService;
    private final LanderCallbackService callbackService;
    private final Map<String, MockProcessInstance> processStore = new ConcurrentHashMap<>();
    private final Map<String, TaskObject> taskStore = new ConcurrentHashMap<>();

    public LanderMockService(ChaosConfigService chaosConfigService, LanderCallbackService callbackService) {
        this.chaosConfigService = chaosConfigService;
        this.callbackService = callbackService;
    }

    public String startProcess(StartProcessRequest req) {
        ChaosConfig config = chaosConfigService.get();
        chaosConfigService.applyDelay(config.landerDelayMs());
        if (chaosConfigService.hitFailRate(config.landerFailRate())) {
            throw new ChaosFaultException("Simulated Lander startProcess failure");
        }

        long nowMs = System.currentTimeMillis();
        String piid = UUID.randomUUID().toString();
        boolean submit = req.getSubmit() == null || req.getSubmit();
        String nodeId = submit ? "step01" : "_BOF";
        String nodeDisplayName = submit ? "初审节点" : "草稿节点";

        MockProcessInstance instance = new MockProcessInstance();
        instance.setPiid(piid);
        instance.setApplyId(req.getApplyId() != null ? req.getApplyId() : UUID.randomUUID().toString());
        instance.setBaseSchema(req.getBaseSchema() != null ? req.getBaseSchema() : "BOP_DEFAULT_FLOW");
        instance.setTitle(req.getTitle());
        instance.setExternalFormUrl(req.getExternalFormUrl());
        instance.setState(PROCESS_STATE_RUN);
        instance.setCurrentNodeId(nodeId);
        instance.setCurrentNodeDisplayName(nodeDisplayName);
        instance.setCreator(req.getHandler());
        if (req.getExtendData() != null) {
            instance.setExtendData(new HashMap<>(req.getExtendData()));
        }
        LocalDateTime now = LocalDateTime.now();
        instance.setCreatedAt(now);
        instance.setUpdatedAt(now);

        List<LanderUserInfo> owners = resolveOwners(req.getNextHandlers(), req.getHandler());
        List<TaskObject> createdTasks = new ArrayList<>();
        for (LanderUserInfo owner : owners) {
            TaskObject task = buildTask(instance, nodeId, nodeDisplayName, true, nowMs, owner);
            createdTasks.add(task);
            instance.getTasks().add(task);
            taskStore.put(task.getTiid(), task);
        }
        processStore.put(piid, instance);

        CallbackPayload payload = buildCallbackPayload(instance, "_BOF", nodeId, nodeDisplayName, true, false,
                nowMs, createdTasks, List.of());
        callbackService.dispatchAsync(payload);
        return piid;
    }

    public CompleteResult completeTask(CompleteTaskRequest req) {
        ChaosConfig config = chaosConfigService.get();
        chaosConfigService.applyDelay(config.landerDelayMs());
        if (chaosConfigService.hitFailRate(config.landerFailRate())) {
            throw new ChaosFaultException("Simulated Lander completeTask failure");
        }
        if (config.landerDuplicateTaskError()) {
            return CompleteResult.alreadyCompleted();
        }
        if (req.getTiid() == null || req.getTiid().isBlank()) {
            return CompleteResult.error(HttpStatus.BAD_REQUEST, "INVALID_PARAM", "tiid is required");
        }
        String decision = req.getDecision() != null ? req.getDecision().toUpperCase() : "YES";
        if (!VALID_DECISIONS.contains(decision)) {
            return CompleteResult.error(HttpStatus.BAD_REQUEST, "INVALID_DECISION", "Unsupported decision: " + decision);
        }

        TaskObject currentTask = taskStore.get(req.getTiid());
        if (currentTask == null) {
            return CompleteResult.error(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "Task not found: " + req.getTiid());
        }
        MockProcessInstance instance = processStore.get(currentTask.getPiid());
        if (instance == null) {
            return CompleteResult.error(HttpStatus.NOT_FOUND, "PROCESS_NOT_FOUND", "Process not found: " + currentTask.getPiid());
        }

        long nowMs = System.currentTimeMillis();
        synchronized (instance) {
            if (currentTask.getState() != TASK_STATE_READY) {
                return CompleteResult.alreadyCompleted();
            }

            String preNodeId = currentTask.getNodeId();
            currentTask.setState(TASK_STATE_COMPLETED);
            currentTask.setLastModified(nowMs);
            if (req.getExtendData() != null) {
                instance.getExtendData().putAll(req.getExtendData());
            }

            List<TaskObject> completedTasks = new ArrayList<>();
            completedTasks.add(currentTask);
            List<TaskObject> nextTasks = new ArrayList<>();
            boolean endFlow = false;
            String nextNodeId;
            String nextNodeName;

            switch (decision) {
                case "CANCEL" -> {
                    endFlow = true;
                    nextNodeId = "_EOF";
                    nextNodeName = "已作废";
                    instance.setState(PROCESS_STATE_END);
                    terminateOpenTasks(instance, nowMs, completedTasks);
                }
                case "REJECT" -> {
                    nextNodeId = "_BOF";
                    nextNodeName = "驳回修改";
                    List<LanderUserInfo> owners = resolveOwners(req.getNextHandlers(), instance.getCreator());
                    createNextTasks(instance, nextNodeId, nextNodeName, nowMs, owners, nextTasks);
                }
                case "JUMP" -> {
                    nextNodeId = (req.getNextNode() != null && !req.getNextNode().isBlank())
                            ? req.getNextNode() : "step_jump";
                    nextNodeName = req.getOption() != null ? req.getOption() : "跳转节点";
                    if ("_EOF".equals(nextNodeId)) {
                        endFlow = true;
                        instance.setState(PROCESS_STATE_END);
                    } else {
                        List<LanderUserInfo> owners = resolveOwners(req.getNextHandlers(), req.getHandler());
                        createNextTasks(instance, nextNodeId, nextNodeName, nowMs, owners, nextTasks);
                    }
                }
                default -> {
                    if ("_EOF".equals(req.getNextNode()) || "step02".equals(preNodeId)) {
                        endFlow = true;
                        nextNodeId = "_EOF";
                        nextNodeName = "归档结束";
                        instance.setState(PROCESS_STATE_END);
                    } else {
                        nextNodeId = "step01".equals(preNodeId) ? "step02" : "step01";
                        nextNodeName = (req.getOption() != null && !req.getOption().isBlank())
                                ? req.getOption() : ("step02".equals(nextNodeId) ? "三级经理审核" : "初审节点");
                        List<LanderUserInfo> owners = resolveOwners(req.getNextHandlers(), req.getHandler());
                        createNextTasks(instance, nextNodeId, nextNodeName, nowMs, owners, nextTasks);
                    }
                }
            }

            instance.setCurrentNodeId(nextNodeId);
            instance.setCurrentNodeDisplayName(nextNodeName);
            instance.setUpdatedAt(LocalDateTime.now());

            CallbackPayload payload = buildCallbackPayload(
                    instance, preNodeId, nextNodeId, nextNodeName, false, endFlow, nowMs, nextTasks, completedTasks);
            callbackService.dispatchAsync(payload);
            return CompleteResult.ok();
        }
    }

    public boolean terminateProcess(TerminateProcessRequest req) {
        ChaosConfig config = chaosConfigService.get();
        chaosConfigService.applyDelay(config.landerDelayMs());
        if (chaosConfigService.hitFailRate(config.landerFailRate())) {
            throw new ChaosFaultException("Simulated Lander terminateProcess failure");
        }
        if (req.getPiid() == null || req.getPiid().isBlank()) {
            return false;
        }
        MockProcessInstance instance = processStore.get(req.getPiid());
        if (instance == null) {
            return false;
        }
        long nowMs = System.currentTimeMillis();
        synchronized (instance) {
            instance.setState(PROCESS_STATE_STOP);
            instance.setTerminateReason(req.getReason());
            instance.setCurrentNodeId("_EOF");
            instance.setCurrentNodeDisplayName("已终止");
            instance.setUpdatedAt(LocalDateTime.now());
            for (TaskObject task : instance.getTasks()) {
                if (task.getState() == TASK_STATE_READY) {
                    task.setState(TASK_STATE_TERMINATED);
                    task.setLastModified(nowMs);
                }
            }
        }
        return true;
    }

    public TaskObject getTask(String tiid) {
        ChaosConfig config = chaosConfigService.get();
        chaosConfigService.applyDelay(config.landerDelayMs());
        if (config.landerReturnNullTask()) {
            return null;
        }
        return taskStore.get(tiid);
    }

    public List<MockProcessInstance> listInstances() {
        return processStore.values().stream()
                .sorted(Comparator.comparing(MockProcessInstance::getCreatedAt).reversed())
                .toList();
    }

    public CallbackLogEntry triggerManualCallback(String piid) {
        MockProcessInstance instance = processStore.get(piid);
        if (instance == null) {
            throw new BizException(404, "Process instance not found: " + piid);
        }
        long nowMs = System.currentTimeMillis();
        List<TaskObject> activeTasks = instance.getTasks().stream()
                .filter(t -> t.getState() == TASK_STATE_READY)
                .toList();
        List<TaskObject> doneTasks = instance.getTasks().stream()
                .filter(t -> t.getState() != TASK_STATE_READY)
                .toList();
        boolean endFlow = instance.getState() == PROCESS_STATE_END || instance.getState() == PROCESS_STATE_STOP;
        CallbackPayload payload = buildCallbackPayload(
                instance,
                "_BOF",
                instance.getCurrentNodeId(),
                instance.getCurrentNodeDisplayName(),
                doneTasks.isEmpty(),
                endFlow,
                nowMs,
                activeTasks,
                doneTasks
        );
        return callbackService.triggerSync(payload);
    }

    private void createNextTasks(
            MockProcessInstance instance,
            String nodeId,
            String nodeName,
            long nowMs,
            List<LanderUserInfo> owners,
            List<TaskObject> collector) {
        for (LanderUserInfo owner : owners) {
            TaskObject nextTask = buildTask(instance, nodeId, nodeName, false, nowMs, owner);
            instance.getTasks().add(nextTask);
            taskStore.put(nextTask.getTiid(), nextTask);
            collector.add(nextTask);
        }
    }

    private void terminateOpenTasks(MockProcessInstance instance, long nowMs, List<TaskObject> collector) {
        for (TaskObject task : instance.getTasks()) {
            if (task.getState() == TASK_STATE_READY) {
                task.setState(TASK_STATE_TERMINATED);
                task.setLastModified(nowMs);
                collector.add(task);
            }
        }
    }

    private List<LanderUserInfo> resolveOwners(List<LanderUserInfo> nextHandlers, LanderUserInfo fallback) {
        List<LanderUserInfo> valid = new ArrayList<>();
        if (nextHandlers != null) {
            for (LanderUserInfo user : nextHandlers) {
                if (user == null || user.getUserId() == null || "<system>".equalsIgnoreCase(user.getUserId())) {
                    continue;
                }
                valid.add(normalizeUser(user));
            }
        }
        if (valid.isEmpty()) {
            if (fallback != null && fallback.getUserId() != null) {
                valid.add(normalizeUser(fallback));
            } else {
                valid.add(new LanderUserInfo("U:mock_approver", "模拟审批人", "DEPT-01", "默认部门", ""));
            }
        }
        return valid;
    }

    private LanderUserInfo normalizeUser(LanderUserInfo source) {
        String uid = source.getUserId();
        if (uid != null && !uid.startsWith("U:") && !uid.startsWith("R:") && !uid.startsWith("G:")) {
            uid = "U:" + uid;
        }
        return new LanderUserInfo(uid, source.getUserName(), source.getDeptId(), source.getDeptName(), source.getUserRole());
    }

    private TaskObject buildTask(
            MockProcessInstance instance,
            String nodeId,
            String nodeDisplayName,
            boolean first,
            long nowMs,
            LanderUserInfo owner) {
        TaskObject task = new TaskObject();
        task.setTiid(UUID.randomUUID().toString());
        task.setPiid(instance.getPiid());
        task.setApplyId(instance.getApplyId());
        task.setTemplateId(instance.getBaseSchema());
        task.setNodeId(nodeId);
        task.setNodeDisplayName(nodeDisplayName);
        task.setState(TASK_STATE_READY);
        task.setFirst(first);
        task.setCreateTime(nowMs);
        task.setLastModified(nowMs);
        task.setApplicant(instance.getCreator());
        task.setOriginalOwner(owner);
        return task;
    }

    private CallbackPayload buildCallbackPayload(
            MockProcessInstance instance,
            String preNodeId,
            String nodeId,
            String nodeDisplayName,
            boolean first,
            boolean endFlow,
            long modifiedTime,
            List<TaskObject> curTasks,
            List<TaskObject> preTasks) {
        List<CallbackPayload.CallbackTask> callbackTasks = curTasks.stream()
                .map(t -> new CallbackPayload.CallbackTask(
                        t.getTiid(), t.getState(), t.isFirst(), t.getCreateTime(), t.getLastModified(), t.getOriginalOwner()))
                .toList();
        List<CallbackPayload.CallbackTask> preCallbackTasks = preTasks.stream()
                .map(t -> new CallbackPayload.CallbackTask(
                        t.getTiid(), t.getState(), t.isFirst(), t.getCreateTime(), t.getLastModified(), t.getOriginalOwner()))
                .toList();
        CallbackPayload.CallbackInfo info = new CallbackPayload.CallbackInfo(
                preNodeId, nodeId, nodeDisplayName, first, callbackTasks, preCallbackTasks);
        return new CallbackPayload(
                instance.getPiid(),
                "BOP_MOCK",
                instance.getBaseSchema(),
                1,
                instance.getBaseSchema(),
                instance.getApplyId(),
                modifiedTime,
                endFlow,
                List.of(info)
        );
    }

    public record CompleteResult(boolean success, HttpStatus status, String errorCode, String errmsg) {
        public static CompleteResult ok() {
            return new CompleteResult(true, HttpStatus.OK, null, null);
        }

        public static CompleteResult alreadyCompleted() {
            return new CompleteResult(false, HttpStatus.CONFLICT, "TASK_ALREADY_COMPLETED", "Task has already been completed");
        }

        public static CompleteResult error(HttpStatus status, String code, String msg) {
            return new CompleteResult(false, status, code, msg);
        }
    }
}
