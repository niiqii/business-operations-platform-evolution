package com.bop.workorder.controller;

import com.bop.workorder.client.dto.LanderCallbackPayload;
import com.bop.workorder.client.dto.LanderCallbackResult;
import com.bop.workorder.service.WorkOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LanderCallbackController {

    private static final Logger log = LoggerFactory.getLogger(LanderCallbackController.class);

    private final WorkOrderService workOrderService;

    public LanderCallbackController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/wfasynch/rs/service/runtime/process/changeState")
    public synchronized LanderCallbackResult changeState(@RequestBody LanderCallbackPayload payload) {
        try {
            workOrderService.handleLanderCallback(payload);
            return LanderCallbackResult.success();
        } catch (Exception ex) {
            log.error("Failed to process Lander callback, piid={}", payload != null ? payload.getPiid() : null, ex);
            return LanderCallbackResult.failure(ex.getMessage());
        }
    }
}
