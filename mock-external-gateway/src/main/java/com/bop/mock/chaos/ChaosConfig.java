package com.bop.mock.chaos;

public record ChaosConfig(
        String landerCallbackUrl,
        String scmCallbackUrl,
        long landerDelayMs,
        double landerFailRate,
        boolean landerDuplicateTaskError,
        boolean landerReturnNullTask,
        int callbackDuplicateCount,
        boolean callbackOutOfOrder,
        boolean smsFailEnabled,
        long smsDelayMs,
        boolean scmPartialFail
) {
}
