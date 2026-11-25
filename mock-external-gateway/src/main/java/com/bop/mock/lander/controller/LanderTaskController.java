package com.bop.mock.lander.controller;

import com.bop.mock.common.ChaosFaultException;
import com.bop.mock.lander.model.CompleteTaskRequest;
import com.bop.mock.lander.model.TaskObject;
import com.bop.mock.lander.service.LanderMockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/UniflowControlCenter/rest/RestTaskService")
public class LanderTaskController {

    private final LanderMockService landerMockService;

    public LanderTaskController(LanderMockService landerMockService) {
        this.landerMockService = landerMockService;
    }

    @PostMapping("/completeTask")
    public ResponseEntity<Map<String, Object>> completeTask(@RequestBody CompleteTaskRequest request) {
        try {
            LanderMockService.CompleteResult result = landerMockService.completeTask(request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", result.success());
            if (result.success()) {
                body.put("result", 1);
                return ResponseEntity.ok(body);
            }
            body.put("errorCode", result.errorCode());
            body.put("errmsg", result.errmsg());
            return ResponseEntity.status(result.status()).body(body);
        } catch (ChaosFaultException ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("errorCode", "LANDER_500");
            body.put("errmsg", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    @GetMapping("/getTask/{tiid}")
    public ResponseEntity<TaskObject> getTask(@PathVariable String tiid) {
        TaskObject task = landerMockService.getTask(tiid);
        return ResponseEntity.ok(task);
    }
}
