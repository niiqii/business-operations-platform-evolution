package com.bop.mock.scm.service;

import com.bop.mock.chaos.ChaosConfigService;
import com.bop.mock.common.BizException;
import com.bop.mock.common.PageResult;
import com.bop.mock.common.TraceIdFilter;
import com.bop.mock.scm.model.ScmBalanceRecord;
import com.bop.mock.scm.model.ScmContractRecord;
import com.bop.mock.scm.model.ScmCostOrderImportRequest;
import com.bop.mock.scm.model.ScmCostOrderImportResponse;
import com.bop.mock.scm.model.ScmOrderCreateRequest;
import com.bop.mock.scm.model.ScmOrderRecord;
import com.bop.mock.scm.model.ScmVendorRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScmMockService {

    private static final Logger log = LoggerFactory.getLogger(ScmMockService.class);

    private final ChaosConfigService chaosConfigService;
    private final ScmDatasetGenerator datasetGenerator;
    private final RestClient callbackRestClient;
    private final AtomicInteger orderSeq = new AtomicInteger(1000);
    private final Map<String, ScmOrderRecord> orderByBillNo = new ConcurrentHashMap<>();
    private final Map<String, String> billNoByBopNo = new ConcurrentHashMap<>();
    private final List<ScmBalanceRecord> balanceRecords = new CopyOnWriteArrayList<>();

    public ScmMockService(
            ChaosConfigService chaosConfigService,
            ScmDatasetGenerator datasetGenerator,
            RestClient callbackRestClient) {
        this.chaosConfigService = chaosConfigService;
        this.datasetGenerator = datasetGenerator;
        this.callbackRestClient = callbackRestClient;
    }

    public ScmOrderRecord createOrder(ScmOrderCreateRequest request) {
        String billNo = "PO-" + orderSeq.incrementAndGet();
        String billNoBop = request.getBillNoBop() != null ? request.getBillNoBop() : "BOP-" + orderSeq.get();
        String taxationCd = request.getTaxationCd() != null ? request.getTaxationCd() : "VAT6";
        String flowNo = request.getFlowNo() != null ? request.getFlowNo() : UUID.randomUUID().toString().replace("-", "");
        List<ScmOrderCreateRequest.PoLine> lines = request.getPoLines() != null
                ? new ArrayList<>(request.getPoLines()) : List.of();

        ScmOrderRecord record = new ScmOrderRecord(
                billNo,
                billNoBop,
                request.getBillName(),
                request.getContractNo(),
                request.getOrderType() != null ? request.getOrderType() : "2",
                taxationCd,
                flowNo,
                request.getBuyerName(),
                "1",
                lines,
                LocalDateTime.now()
        );
        orderByBillNo.put(billNo, record);
        billNoByBopNo.put(billNoBop, billNo);
        return record;
    }

    public Map<String, Object> triggerOrderReleaseCallback(String billNoOrBopNo) {
        ScmOrderRecord order = orderByBillNo.get(billNoOrBopNo);
        if (order == null) {
            String mapped = billNoByBopNo.get(billNoOrBopNo);
            if (mapped != null) {
                order = orderByBillNo.get(mapped);
            }
        }
        if (order == null) {
            throw new BizException(404, "SCM order not found: " + billNoOrBopNo);
        }

        long nowMs = System.currentTimeMillis();
        String billNo = order.billNo();
        List<ScmOrderRecord.CallbackLine> callbackLines = order.poLines().stream()
                .map(line -> new ScmOrderRecord.CallbackLine(
                        billNo,
                        line.getRowId() != null ? String.valueOf(line.getRowId()) : "1",
                        line.getItemCd() != null ? line.getItemCd() : "ITEM-001",
                        line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO,
                        "项",
                        line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ONE,
                        line.getUnitPrice2() != null ? line.getUnitPrice2() : new BigDecimal("1.06"),
                        line.getUserName(),
                        line.getUserName(),
                        line.getRcptStartDate(),
                        line.getRcptDate(),
                        line.getProjectCd(),
                        line.getTaskCd(),
                        line.getTaxationCd() != null ? line.getTaxationCd() : "VAT6"
                ))
                .toList();

        ScmOrderRecord.CallbackPayload payload = new ScmOrderRecord.CallbackPayload(
                order.billNo(),
                order.billName(),
                order.orderType(),
                order.contractNo(),
                "VND-00001",
                "脱敏服务商-1",
                order.buyerName(),
                nowMs - 60_000L,
                nowMs,
                order.billNoBop(),
                "8",
                UUID.randomUUID().toString().replace("-", ""),
                nowMs,
                callbackLines
        );

        String targetUrl = chaosConfigService.get().scmCallbackUrl();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("billNo", order.billNo());
        result.put("billNoBop", order.billNoBop());
        result.put("targetUrl", targetUrl);
        result.put("reference3", "8");

        try {
            RestClient.RequestBodySpec req = callbackRestClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON);
            if (traceId != null && !traceId.isBlank()) {
                req = req.header(TraceIdFilter.TRACE_ID_KEY, traceId);
            }
            ResponseEntity<String> resp = req.body(payload).retrieve().toEntity(String.class);
            result.put("httpStatus", resp.getStatusCode().value());
            result.put("success", resp.getStatusCode().is2xxSuccessful());
            result.put("responseBody", resp.getBody());
            orderByBillNo.put(order.billNo(), new ScmOrderRecord(
                    order.billNo(), order.billNoBop(), order.billName(), order.contractNo(),
                    order.orderType(), order.taxationCd(), order.flowNo(), order.buyerName(),
                    "8", order.poLines(), order.createdAt()
            ));
        } catch (RestClientResponseException ex) {
            log.warn("SCM order callback rejected, billNo={}, url={}, status={}",
                    order.billNo(), targetUrl, ex.getStatusCode().value());
            result.put("httpStatus", ex.getStatusCode().value());
            result.put("success", false);
            result.put("responseBody", ex.getResponseBodyAsString());
            result.put("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            log.warn("SCM order callback failed, billNo={}, url={}, err={}", order.billNo(), targetUrl, ex.getMessage());
            result.put("httpStatus", 0);
            result.put("success", false);
            result.put("errorMessage", ex.getMessage());
        }
        return result;
    }

    public ScmCostOrderImportResponse importCostOrder(ScmCostOrderImportRequest request) {
        List<ScmCostOrderImportRequest.CostOrderLine> lines = request.getLines() != null
                ? request.getLines() : List.of();
        boolean partialFail = chaosConfigService.get().scmPartialFail();
        List<ScmCostOrderImportResponse.LineError> errors = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < lines.size(); i++) {
            ScmCostOrderImportRequest.CostOrderLine line = lines.get(i);
            boolean shouldFailLine = partialFail && (lines.size() == 1 || i % 2 == 1);
            if (shouldFailLine) {
                errors.add(new ScmCostOrderImportResponse.LineError(
                        line.getPoBillNo(),
                        line.getLineRowId() != null ? line.getLineRowId() : (i + 1),
                        "Simulated partial failure on line " + (line.getLineRowId() != null ? line.getLineRowId() : (i + 1))
                ));
            } else {
                // 请款成功行自动写入内存对账列表，供 /scm/balance/query 闭环联调
                BigDecimal settleAmt = line.getSettleAmount() != null ? line.getSettleAmount() : BigDecimal.ZERO;
                BigDecimal deductAmt = line.getDeductionAmount() != null ? line.getDeductionAmount() : BigDecimal.ZERO;
                BigDecimal netAmount = settleAmt.add(deductAmt);
                balanceRecords.add(new ScmBalanceRecord(
                        line.getExBillLineNo() != null ? line.getExBillLineNo() : request.getExBillNo() + "_" + (i + 1),
                        request.getExBillNo(),
                        line.getPoBillNo(),
                        line.getLineRowId() != null ? line.getLineRowId() : (i + 1),
                        line.getSettleMonth(),
                        "APPROVED",
                        netAmount,
                        now,
                        now
                ));
            }
        }

        if (!errors.isEmpty()) {
            return new ScmCostOrderImportResponse("3", "部分请款明细导入失败", lines.size(), errors);
        }
        return new ScmCostOrderImportResponse("1", "全部请款明细导入成功", lines.size(), List.of());
    }

    public PageResult<ScmContractRecord> queryContracts(
            LocalDateTime lastUpdateFrom, LocalDateTime lastUpdateTo, int pageNo, int pageSize) {
        return datasetGenerator.queryContracts(lastUpdateFrom, lastUpdateTo, pageNo, pageSize);
    }

    public PageResult<ScmVendorRecord> queryVendors(
            LocalDateTime lastUpdateFrom, LocalDateTime lastUpdateTo, int pageNo, int pageSize) {
        return datasetGenerator.queryVendors(lastUpdateFrom, lastUpdateTo, pageNo, pageSize);
    }

    public PageResult<ScmBalanceRecord> queryBalances(
            LocalDateTime lastUpdateFrom, LocalDateTime lastUpdateTo, String billNo, int pageNo, int pageSize) {
        int safePageNo = Math.max(1, pageNo);
        int safePageSize = Math.max(1, Math.min(pageSize, 500));
        List<ScmBalanceRecord> filtered = balanceRecords.stream()
                .filter(b -> billNo == null || billNo.isBlank()
                        || billNo.equals(b.billNo()) || billNo.equals(b.poBillNo()))
                .filter(b -> lastUpdateFrom == null || !b.lastUpdateTime().isBefore(lastUpdateFrom))
                .filter(b -> lastUpdateTo == null || !b.lastUpdateTime().isAfter(lastUpdateTo))
                .sorted(Comparator.comparing(ScmBalanceRecord::lastUpdateTime).reversed())
                .toList();

        long total = filtered.size();
        int fromIdx = (safePageNo - 1) * safePageSize;
        if (fromIdx >= filtered.size()) {
            return new PageResult<>(safePageNo, safePageSize, total, List.of());
        }
        int toIdx = Math.min(filtered.size(), fromIdx + safePageSize);
        return new PageResult<>(safePageNo, safePageSize, total, filtered.subList(fromIdx, toIdx));
    }

    public List<ScmOrderRecord> listOrders() {
        return orderByBillNo.values().stream()
                .sorted(Comparator.comparing(ScmOrderRecord::createdAt).reversed())
                .toList();
    }
}
