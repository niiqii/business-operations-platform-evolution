package com.bop.mock.scm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class ScmCostOrderImportRequest {

    @JsonProperty("SOURCESYSTEMID")
    private String sourceSystemId;

    @JsonProperty("SOURCESYSTEMNAME")
    private String sourceSystemName;

    @JsonProperty("USERID")
    private String userId;

    @JsonProperty("USERNAME")
    private String userName;

    @JsonProperty("SUBMITDATE")
    private String submitDate;

    @JsonProperty("EX__BILL_NO")
    @JsonAlias({"exBillNo", "EX_BILL_NO"})
    private String exBillNo;

    @JsonProperty("importCostOrderToSCMTLines")
    @JsonAlias({"lines", "costLines"})
    private List<CostOrderLine> lines;

    public String getSourceSystemId() {
        return sourceSystemId;
    }

    public void setSourceSystemId(String sourceSystemId) {
        this.sourceSystemId = sourceSystemId;
    }

    public String getSourceSystemName() {
        return sourceSystemName;
    }

    public void setSourceSystemName(String sourceSystemName) {
        this.sourceSystemName = sourceSystemName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    public String getExBillNo() {
        return exBillNo;
    }

    public void setExBillNo(String exBillNo) {
        this.exBillNo = exBillNo;
    }

    public List<CostOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<CostOrderLine> lines) {
        this.lines = lines;
    }

    public static class CostOrderLine {

        @JsonProperty("EX_BILL_LINE_NO")
        @JsonAlias("exBillLineNo")
        private String exBillLineNo;

        @JsonProperty("PO_bill_no")
        @JsonAlias({"po_bill_no", "poBillNo"})
        private String poBillNo;

        @JsonProperty("line_row_id")
        @JsonAlias({"Line_row_id", "lineRowId"})
        private Integer lineRowId;

        @JsonProperty("settle_month")
        @JsonAlias("settleMonth")
        private String settleMonth;

        @JsonProperty("settle_hours")
        @JsonAlias("settleHours")
        private BigDecimal settleHours;

        @JsonProperty("settle_amount")
        @JsonAlias("settleAmount")
        private BigDecimal settleAmount;

        @JsonProperty("deduction_amount")
        @JsonAlias("deductionAmount")
        private BigDecimal deductionAmount;

        @JsonProperty("EXAMINE_SCORE")
        private BigDecimal examineScore;

        @JsonProperty("EXAMINE_DEDUCTION")
        private BigDecimal examineDeduction;

        @JsonProperty("REFERENCE1")
        private String reference1;

        @JsonProperty("REFERENCE2")
        private String reference2;

        @JsonProperty("REFERENCE3")
        private String reference3;

        @JsonProperty("REFERENCE4")
        private String reference4;

        @JsonProperty("REFERENCE5")
        private String reference5;

        public String getExBillLineNo() {
            return exBillLineNo;
        }

        public void setExBillLineNo(String exBillLineNo) {
            this.exBillLineNo = exBillLineNo;
        }

        public String getPoBillNo() {
            return poBillNo;
        }

        public void setPoBillNo(String poBillNo) {
            this.poBillNo = poBillNo;
        }

        public Integer getLineRowId() {
            return lineRowId;
        }

        public void setLineRowId(Integer lineRowId) {
            this.lineRowId = lineRowId;
        }

        public String getSettleMonth() {
            return settleMonth;
        }

        public void setSettleMonth(String settleMonth) {
            this.settleMonth = settleMonth;
        }

        public BigDecimal getSettleHours() {
            return settleHours;
        }

        public void setSettleHours(BigDecimal settleHours) {
            this.settleHours = settleHours;
        }

        public BigDecimal getSettleAmount() {
            return settleAmount;
        }

        public void setSettleAmount(BigDecimal settleAmount) {
            this.settleAmount = settleAmount;
        }

        public BigDecimal getDeductionAmount() {
            return deductionAmount;
        }

        public void setDeductionAmount(BigDecimal deductionAmount) {
            this.deductionAmount = deductionAmount;
        }

        public BigDecimal getExamineScore() {
            return examineScore;
        }

        public void setExamineScore(BigDecimal examineScore) {
            this.examineScore = examineScore;
        }

        public BigDecimal getExamineDeduction() {
            return examineDeduction;
        }

        public void setExamineDeduction(BigDecimal examineDeduction) {
            this.examineDeduction = examineDeduction;
        }

        public String getReference1() {
            return reference1;
        }

        public void setReference1(String reference1) {
            this.reference1 = reference1;
        }

        public String getReference2() {
            return reference2;
        }

        public void setReference2(String reference2) {
            this.reference2 = reference2;
        }

        public String getReference3() {
            return reference3;
        }

        public void setReference3(String reference3) {
            this.reference3 = reference3;
        }

        public String getReference4() {
            return reference4;
        }

        public void setReference4(String reference4) {
            this.reference4 = reference4;
        }

        public String getReference5() {
            return reference5;
        }

        public void setReference5(String reference5) {
            this.reference5 = reference5;
        }
    }
}
