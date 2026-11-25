package com.bop.workorder.enums;

public enum BizType {
    WORKHOUR("BOP_WORKHOUR_FLOW"),
    ASSESSMENT("BOP_ASSESSMENT_FLOW");

    private final String baseSchema;

    BizType(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getBaseSchema() {
        return baseSchema;
    }

    public static BizType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BizType type : values()) {
            if (type.name().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}
