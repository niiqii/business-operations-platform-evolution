package com.bop.workorder.common;

import org.slf4j.MDC;

public class ApiResult<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public ApiResult() {
    }

    public ApiResult(int code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<T>(0, "success", data, MDC.get(TraceIdFilter.TRACE_ID_KEY));
    }

    public static ApiResult<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<T>(code, message, null, MDC.get(TraceIdFilter.TRACE_ID_KEY));
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
