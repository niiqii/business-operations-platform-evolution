package com.bop.mock.chaos;

public class ChaosConfigUpdateRequest {

    private String landerCallbackUrl;
    private String scmCallbackUrl;
    private Long landerDelayMs;
    private Double landerFailRate;
    private Boolean landerDuplicateTaskError;
    private Boolean landerReturnNullTask;
    private Integer callbackDuplicateCount;
    private Boolean callbackOutOfOrder;
    private Boolean smsFailEnabled;
    private Long smsDelayMs;
    private Boolean scmPartialFail;

    public String getLanderCallbackUrl() {
        return landerCallbackUrl;
    }

    public void setLanderCallbackUrl(String landerCallbackUrl) {
        this.landerCallbackUrl = landerCallbackUrl;
    }

    public String getScmCallbackUrl() {
        return scmCallbackUrl;
    }

    public void setScmCallbackUrl(String scmCallbackUrl) {
        this.scmCallbackUrl = scmCallbackUrl;
    }

    public Long getLanderDelayMs() {
        return landerDelayMs;
    }

    public void setLanderDelayMs(Long landerDelayMs) {
        this.landerDelayMs = landerDelayMs;
    }

    public Double getLanderFailRate() {
        return landerFailRate;
    }

    public void setLanderFailRate(Double landerFailRate) {
        this.landerFailRate = landerFailRate;
    }

    public Boolean getLanderDuplicateTaskError() {
        return landerDuplicateTaskError;
    }

    public void setLanderDuplicateTaskError(Boolean landerDuplicateTaskError) {
        this.landerDuplicateTaskError = landerDuplicateTaskError;
    }

    public Boolean getLanderReturnNullTask() {
        return landerReturnNullTask;
    }

    public void setLanderReturnNullTask(Boolean landerReturnNullTask) {
        this.landerReturnNullTask = landerReturnNullTask;
    }

    public Integer getCallbackDuplicateCount() {
        return callbackDuplicateCount;
    }

    public void setCallbackDuplicateCount(Integer callbackDuplicateCount) {
        this.callbackDuplicateCount = callbackDuplicateCount;
    }

    public Boolean getCallbackOutOfOrder() {
        return callbackOutOfOrder;
    }

    public void setCallbackOutOfOrder(Boolean callbackOutOfOrder) {
        this.callbackOutOfOrder = callbackOutOfOrder;
    }

    public Boolean getSmsFailEnabled() {
        return smsFailEnabled;
    }

    public void setSmsFailEnabled(Boolean smsFailEnabled) {
        this.smsFailEnabled = smsFailEnabled;
    }

    public Long getSmsDelayMs() {
        return smsDelayMs;
    }

    public void setSmsDelayMs(Long smsDelayMs) {
        this.smsDelayMs = smsDelayMs;
    }

    public Boolean getScmPartialFail() {
        return scmPartialFail;
    }

    public void setScmPartialFail(Boolean scmPartialFail) {
        this.scmPartialFail = scmPartialFail;
    }
}
