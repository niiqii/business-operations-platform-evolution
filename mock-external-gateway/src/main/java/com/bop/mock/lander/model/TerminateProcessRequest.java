package com.bop.mock.lander.model;

public class TerminateProcessRequest {

    private String piid;
    private String reason;

    public String getPiid() {
        return piid;
    }

    public void setPiid(String piid) {
        this.piid = piid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
