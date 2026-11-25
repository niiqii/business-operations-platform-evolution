package com.bop.mock.notify;

import com.bop.mock.common.ApiResult;
import com.bop.mock.common.MockAdminApi;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@MockAdminApi
@RestController
@RequestMapping("/mock/notify")
public class NotifyMockController {

    private final NotifyMockService notifyMockService;

    public NotifyMockController(NotifyMockService notifyMockService) {
        this.notifyMockService = notifyMockService;
    }

    @PostMapping("/send")
    public ApiResult<NotifyRecord> send(@Valid @RequestBody NotifySendRequest request) {
        return ApiResult.ok(notifyMockService.send(request));
    }

    @GetMapping("/records")
    public ApiResult<List<NotifyRecord>> records(
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResult.ok(notifyMockService.listRecent(channel, limit));
    }

    @DeleteMapping("/records")
    public ApiResult<Void> clear() {
        notifyMockService.clear();
        return ApiResult.ok();
    }
}
