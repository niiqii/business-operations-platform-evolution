package com.bop.workorder.common;

import org.springframework.util.StringUtils;

public final class UserContext {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String DEFAULT_USER_ID = "system";

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<String>();

    private UserContext() {
    }

    public static void setUserId(String userId) {
        HOLDER.set(StringUtils.hasText(userId) ? userId.trim() : DEFAULT_USER_ID);
    }

    public static String getUserId() {
        String userId = HOLDER.get();
        return StringUtils.hasText(userId) ? userId : DEFAULT_USER_ID;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
