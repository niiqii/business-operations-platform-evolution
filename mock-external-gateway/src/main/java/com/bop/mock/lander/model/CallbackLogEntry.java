package com.bop.mock.lander.model;

import java.time.LocalDateTime;

public record CallbackLogEntry(
        String logId,
        String piid,
        String applyId,
        String targetUrl,
        long modifiedTime,
        int httpStatus,
        boolean success,
        long durationMs,
        String responseBody,
        String errorMessage,
        String traceId,
        String payloadJson,
        LocalDateTime createdAt
) {
}
