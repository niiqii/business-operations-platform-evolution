package com.bop.workorder.dto;

import javax.validation.constraints.NotBlank;

public class WorkOrderApproveRequest {

    @NotBlank(message = "decision is required (YES or REJECT)")
    private String decision;

    private String nextHandler;
    private String nextHandlerName;
    private String remark;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getNextHandler() {
        return nextHandler;
    }

    public void setNextHandler(String nextHandler) {
        this.nextHandler = nextHandler;
    }

    public String getNextHandlerName() {
        return nextHandlerName;
    }

    public void setNextHandlerName(String nextHandlerName) {
        this.nextHandlerName = nextHandlerName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
