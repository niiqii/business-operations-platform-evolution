package com.bop.mock.scm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScmContractRecord(
        String contractCode,
        String contractName,
        String vendorCode,
        String vendorName,
        String frameFlag,
        BigDecimal contractAmount,
        BigDecimal accountAmount,
        LocalDate startDateActive,
        LocalDate endDateActive,
        String status,
        LocalDateTime lastUpdateTime
) {
}
