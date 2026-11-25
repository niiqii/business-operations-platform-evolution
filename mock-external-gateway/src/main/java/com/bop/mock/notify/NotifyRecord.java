package com.bop.mock.notify;

import java.time.LocalDateTime;

public record NotifyRecord(
        String id,
        String channel,
        String recipient,
        String title,
        String content,
        String bizId,
        String templateCode,
        String status,
        String errorMessage,
        String traceId,
        LocalDateTime createdAt
) {
}
