package com.bop.mock.scm.model;

import java.time.LocalDateTime;

public record ScmVendorRecord(
        String vendorCode,
        String vendorName,
        String primaryVendorId,
        String taxRegNo,
        String regionCode,
        String status,
        LocalDateTime lastUpdateTime
) {
}
