package com.bop.mock.scm.model;

import java.math.BigDecimal;
import java.util.List;

public class ScmOrderCreateRequest {

    private String billName;
    private String billNoBop;
    private String contractNo;
    private String orderType;
    private String flowNo;
    private Long sendTime;
    private String buyerName;
    private String notes;
    private String taxationCd;
    private List<PoLine> poLines;

    public String getBillName() {
        return billName;
    }

    public void setBillName(String billName) {
        this.billName = billName;
    }

    public String getBillNoBop() {
        return billNoBop;
    }

    public void setBillNoBop(String billNoBop) {
        this.billNoBop = billNoBop;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getFlowNo() {
        return flowNo;
    }

    public void setFlowNo(String flowNo) {
        this.flowNo = flowNo;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTaxationCd() {
        return taxationCd;
    }

    public void setTaxationCd(String taxationCd) {
        this.taxationCd = taxationCd;
    }

    public List<PoLine> getPoLines() {
        return poLines;
    }

    public void setPoLines(List<PoLine> poLines) {
        this.poLines = poLines;
    }

    public static class PoLine {

        private String billNoBop;
        private Integer rowId;
        private BigDecimal quantity;
        private String taxationCd;
        private BigDecimal unitPrice;
        private BigDecimal unitPrice2;
        private Long rcptStartDate;
        private Long rcptDate;
        private String projectCd;
        private String itemCd;
        private String taskCd;
        private String userName;

        public String getBillNoBop() {
            return billNoBop;
        }

        public void setBillNoBop(String billNoBop) {
            this.billNoBop = billNoBop;
        }

        public Integer getRowId() {
            return rowId;
        }

        public void setRowId(Integer rowId) {
            this.rowId = rowId;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getTaxationCd() {
            return taxationCd;
        }

        public void setTaxationCd(String taxationCd) {
            this.taxationCd = taxationCd;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getUnitPrice2() {
            return unitPrice2;
        }

        public void setUnitPrice2(BigDecimal unitPrice2) {
            this.unitPrice2 = unitPrice2;
        }

        public Long getRcptStartDate() {
            return rcptStartDate;
        }

        public void setRcptStartDate(Long rcptStartDate) {
            this.rcptStartDate = rcptStartDate;
        }

        public Long getRcptDate() {
            return rcptDate;
        }

        public void setRcptDate(Long rcptDate) {
            this.rcptDate = rcptDate;
        }

        public String getProjectCd() {
            return projectCd;
        }

        public void setProjectCd(String projectCd) {
            this.projectCd = projectCd;
        }

        public String getItemCd() {
            return itemCd;
        }

        public void setItemCd(String itemCd) {
            this.itemCd = itemCd;
        }

        public String getTaskCd() {
            return taskCd;
        }

        public void setTaskCd(String taskCd) {
            this.taskCd = taskCd;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
