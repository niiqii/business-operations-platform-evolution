package com.bop.mock.scm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScmBalanceRecord(
        String exBillLineNo,
        String billNo,
        String poBillNo,
        Integer lineRowId,
        String settleMonth,
        String applyStatus,
        BigDecimal amount,
        LocalDateTime balanceTime,
        LocalDateTime lastUpdateTime
) {
}
