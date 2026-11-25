package com.bop.mock.lander.controller;

import com.bop.mock.common.ChaosFaultException;
import com.bop.mock.lander.model.StartProcessRequest;
import com.bop.mock.lander.model.TerminateProcessRequest;
import com.bop.mock.lander.service.LanderMockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/UniflowControlCenter/rest/RestProcessService")
public class LanderProcessController {

    private final LanderMockService landerMockService;

    public LanderProcessController(LanderMockService landerMockService) {
        this.landerMockService = landerMockService;
    }

    @PostMapping("/startProcess")
    public ResponseEntity<Map<String, Object>> startProcess(@RequestBody StartProcessRequest request) {
        try {
            String piid = landerMockService.startProcess(request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("piid", piid);
            return ResponseEntity.ok(body);
        } catch (ChaosFaultException ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "LANDER_500", ex.getMessage());
        }
    }

    @PostMapping("/terminateProcess")
    public ResponseEntity<Map<String, Object>> terminateProcess(@RequestBody TerminateProcessRequest request) {
        try {
            boolean terminated = landerMockService.terminateProcess(request);
            if (!terminated) {
                return errorResponse(HttpStatus.NOT_FOUND, "PROCESS_NOT_FOUND", "Process not found: " + request.getPiid());
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", true);
            body.put("piid", request.getPiid());
            return ResponseEntity.ok(body);
        } catch (ChaosFaultException ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "LANDER_500", ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String code, String msg) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("errorCode", code);
        body.put("errmsg", msg);
        return ResponseEntity.status(status).body(body);
    }
}
