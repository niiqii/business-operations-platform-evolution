package com.bop.mock.lander.controller;

import com.bop.mock.common.ApiResult;
import com.bop.mock.common.MockAdminApi;
import com.bop.mock.lander.model.CallbackLogEntry;
import com.bop.mock.lander.model.MockProcessInstance;
import com.bop.mock.lander.service.LanderCallbackService;
import com.bop.mock.lander.service.LanderMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@MockAdminApi
@RestController
@RequestMapping("/mock/lander")
public class LanderMockAdminController {

    private final LanderMockService landerMockService;
    private final LanderCallbackService callbackService;

    public LanderMockAdminController(LanderMockService landerMockService, LanderCallbackService callbackService) {
        this.landerMockService = landerMockService;
        this.callbackService = callbackService;
    }

    @GetMapping("/instances")
    public ApiResult<List<MockProcessInstance>> listInstances() {
        return ApiResult.ok(landerMockService.listInstances());
    }

    @PostMapping("/callback/trigger/{piid}")
    public ApiResult<CallbackLogEntry> triggerCallback(@PathVariable String piid) {
        return ApiResult.ok(landerMockService.triggerManualCallback(piid));
    }

    @GetMapping("/callbacks")
    public ApiResult<List<CallbackLogEntry>> listCallbacks(@RequestParam(defaultValue = "50") int limit) {
        return ApiResult.ok(callbackService.listLogs(limit));
    }

    @PostMapping("/callback/replay/{logId}")
    public ApiResult<CallbackLogEntry> replayCallback(@PathVariable String logId) {
        return ApiResult.ok(callbackService.replay(logId));
    }
}
