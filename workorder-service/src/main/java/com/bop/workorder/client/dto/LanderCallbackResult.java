package com.bop.workorder.client.dto;

public class LanderCallbackResult {

    private boolean ok;
    private String returnValue;
    private String exceptionMessage;

    public LanderCallbackResult() {
    }

    public LanderCallbackResult(boolean ok, String returnValue, String exceptionMessage) {
        this.ok = ok;
        this.returnValue = returnValue;
        this.exceptionMessage = exceptionMessage;
    }

    public static LanderCallbackResult success() {
        return new LanderCallbackResult(true, "0", null);
    }

    public static LanderCallbackResult failure(String message) {
        return new LanderCallbackResult(false, "1", message);
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
