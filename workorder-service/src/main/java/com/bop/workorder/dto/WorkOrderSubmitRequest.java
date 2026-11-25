package com.bop.workorder.dto;

import javax.validation.constraints.NotBlank;

public class WorkOrderSubmitRequest {

    @NotBlank(message = "extJson is required")
    private String extJson;

    @NotBlank(message = "approverId is required")
    private String approverId;

    private String approverName;
    private String remark;

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
