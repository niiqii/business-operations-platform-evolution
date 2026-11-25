package com.bop.mock.scm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ScmCostOrderImportResponse(
        @JsonProperty("ERROR_FLAG") String errorFlag,
        @JsonProperty("ERROR_MESSAGE") String errorMessage,
        @JsonProperty("TOTAL_COUNT") int totalCount,
        List<LineError> responseCollection
) {
    public record LineError(
            @JsonProperty("po_bill_no") String poBillNo,
            @JsonProperty("line_row_id") Integer lineRowId,
            @JsonProperty("ERROR_MESSAGE") String errorMessage
    ) {
    }
}
