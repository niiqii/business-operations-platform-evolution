package com.bop.mock.scm.controller;

import com.bop.mock.common.PageResult;
import com.bop.mock.scm.model.ScmBalanceRecord;
import com.bop.mock.scm.model.ScmContractRecord;
import com.bop.mock.scm.model.ScmCostOrderImportRequest;
import com.bop.mock.scm.model.ScmCostOrderImportResponse;
import com.bop.mock.scm.model.ScmOrderCreateRequest;
import com.bop.mock.scm.model.ScmOrderRecord;
import com.bop.mock.scm.model.ScmVendorRecord;
import com.bop.mock.scm.service.ScmMockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/scm")
public class ScmProtocolController {

    private final ScmMockService scmMockService;

    public ScmProtocolController(ScmMockService scmMockService) {
        this.scmMockService = scmMockService;
    }

    @PostMapping("/order/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody ScmOrderCreateRequest request) {
        ScmOrderRecord created = scmMockService.createOrder(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", 0);
        body.put("msg", "操作成功");
        body.put("billNo", created.billNo());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/costOrder/import")
    public ResponseEntity<ScmCostOrderImportResponse> importCostOrder(@RequestBody ScmCostOrderImportRequest request) {
        return ResponseEntity.ok(scmMockService.importCostOrder(request));
    }

    @GetMapping("/contract/query")
    public ResponseEntity<PageResult<ScmContractRecord>> queryContracts(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateTo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(scmMockService.queryContracts(lastUpdateFrom, lastUpdateTo, pageNo, pageSize));
    }

    @GetMapping("/vendor/query")
    public ResponseEntity<PageResult<ScmVendorRecord>> queryVendors(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateTo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(scmMockService.queryVendors(lastUpdateFrom, lastUpdateTo, pageNo, pageSize));
    }

    @GetMapping("/balance/query")
    public ResponseEntity<PageResult<ScmBalanceRecord>> queryBalances(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastUpdateTo,
            @RequestParam(required = false) String billNo,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(scmMockService.queryBalances(lastUpdateFrom, lastUpdateTo, billNo, pageNo, pageSize));
    }
}
