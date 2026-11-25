package com.bop.mock.scm.controller;

import com.bop.mock.common.ApiResult;
import com.bop.mock.common.MockAdminApi;
import com.bop.mock.scm.model.ScmOrderRecord;
import com.bop.mock.scm.service.ScmMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@MockAdminApi
@RestController
@RequestMapping("/mock/scm")
public class ScmMockAdminController {

    private final ScmMockService scmMockService;

    public ScmMockAdminController(ScmMockService scmMockService) {
        this.scmMockService = scmMockService;
    }

    @GetMapping("/orders")
    public ApiResult<List<ScmOrderRecord>> listOrders() {
        return ApiResult.ok(scmMockService.listOrders());
    }

    @PostMapping("/order/callback/{billNo}")
    public ApiResult<Map<String, Object>> triggerOrderCallback(@PathVariable String billNo) {
        return ApiResult.ok(scmMockService.triggerOrderReleaseCallback(billNo));
    }
}
