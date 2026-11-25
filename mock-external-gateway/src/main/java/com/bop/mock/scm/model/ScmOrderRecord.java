package com.bop.mock.scm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ScmOrderRecord(
        String billNo,
        String billNoBop,
        String billName,
        String contractNo,
        String orderType,
        String taxationCd,
        String flowNo,
        String buyerName,
        String reference3,
        List<ScmOrderCreateRequest.PoLine> poLines,
        LocalDateTime createdAt
) {
    public record CallbackPayload(
            String billNo,
            String billName,
            String orderType,
            String contractNo,
            String supplierCd,
            String supplierName,
            String buyerName,
            long submitedDate,
            long approvedDate,
            String reference1,
            String reference3,
            String flowNo,
            long sendTime,
            List<CallbackLine> poLines
    ) {
    }

    public record CallbackLine(
            String billNo,
            String rowId,
            String itemCd,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            BigDecimal unitPrice2,
            String userName,
            String fullName,
            Long rcptStartDate,
            Long rcptDate,
            String projectCd,
            String taskCd,
            String taxationCd
    ) {
    }
}
