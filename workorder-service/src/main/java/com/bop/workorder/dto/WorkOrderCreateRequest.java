package com.bop.workorder.dto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class WorkOrderCreateRequest {

    @NotBlank(message = "orderNo is required")
    private String orderNo;

    @NotBlank(message = "activityNo is required")
    private String activityNo;

    @NotBlank(message = "vendorId is required")
    private String vendorId;

    @NotBlank(message = "cityCode is required")
    private String cityCode;

    @NotBlank(message = "settlePeriod is required")
    private String settlePeriod;

    @NotBlank(message = "bizType is required")
    private String bizType;

    private Integer schemaVersion = 1;

    @NotBlank(message = "vendorHandler is required")
    private String vendorHandler;

    private String vendorHandlerName;
    private LocalDateTime slaDeadline;
    private String remark;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getActivityNo() {
        return activityNo;
    }

    public void setActivityNo(String activityNo) {
        this.activityNo = activityNo;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getSettlePeriod() {
        return settlePeriod;
    }

    public void setSettlePeriod(String settlePeriod) {
        this.settlePeriod = settlePeriod;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getVendorHandler() {
        return vendorHandler;
    }

    public void setVendorHandler(String vendorHandler) {
        this.vendorHandler = vendorHandler;
    }

    public String getVendorHandlerName() {
        return vendorHandlerName;
    }

    public void setVendorHandlerName(String vendorHandlerName) {
        this.vendorHandlerName = vendorHandlerName;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
