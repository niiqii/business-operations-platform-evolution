package com.bop.mock.common;

import org.slf4j.MDC;

public record ApiResult<T>(int code, String message, T data, String traceId) {

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "success", data, MDC.get(TraceIdFilter.TRACE_ID_KEY));
    }

    public static ApiResult<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null, MDC.get(TraceIdFilter.TRACE_ID_KEY));
    }
}
