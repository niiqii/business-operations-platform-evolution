package com.bop.mock.chaos;

import com.bop.mock.common.ApiResult;
import com.bop.mock.common.MockAdminApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@MockAdminApi
@RestController
@RequestMapping("/mock/chaos")
public class ChaosController {

    private final ChaosConfigService chaosConfigService;

    public ChaosController(ChaosConfigService chaosConfigService) {
        this.chaosConfigService = chaosConfigService;
    }

    @GetMapping("/config")
    public ApiResult<ChaosConfig> getConfig() {
        return ApiResult.ok(chaosConfigService.get());
    }

    @PostMapping("/config")
    public ApiResult<ChaosConfig> updateConfig(@RequestBody ChaosConfigUpdateRequest request) {
        return ApiResult.ok(chaosConfigService.update(request));
    }
}
