package com.bop.mock.chaos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChaosConfigService {

    private final AtomicReference<ChaosConfig> configRef;

    public ChaosConfigService(
            @Value("${mock.lander.callback.default-url:http://localhost:8081/wfasynch/rs/service/runtime/process/changeState}") String landerCallbackUrl,
            @Value("${mock.scm.callback.default-url:http://localhost:8084/scm/order/callback}") String scmCallbackUrl) {
        this.configRef = new AtomicReference<>(new ChaosConfig(
                landerCallbackUrl,
                scmCallbackUrl,
                0L,
                0.0d,
                false,
                false,
                1,
                false,
                false,
                0L,
                false
        ));
    }

    public ChaosConfig get() {
        return configRef.get();
    }

    public ChaosConfig update(ChaosConfigUpdateRequest req) {
        // 原子替换不可变配置快照，避免并发请求读到半更新状态
        return configRef.updateAndGet(cur -> new ChaosConfig(
                nonBlankOrDefault(req.getLanderCallbackUrl(), cur.landerCallbackUrl()),
                nonBlankOrDefault(req.getScmCallbackUrl(), cur.scmCallbackUrl()),
                req.getLanderDelayMs() != null ? Math.max(0L, req.getLanderDelayMs()) : cur.landerDelayMs(),
                req.getLanderFailRate() != null ? clampRate(req.getLanderFailRate()) : cur.landerFailRate(),
                req.getLanderDuplicateTaskError() != null ? req.getLanderDuplicateTaskError() : cur.landerDuplicateTaskError(),
                req.getLanderReturnNullTask() != null ? req.getLanderReturnNullTask() : cur.landerReturnNullTask(),
                req.getCallbackDuplicateCount() != null ? Math.max(1, req.getCallbackDuplicateCount()) : cur.callbackDuplicateCount(),
                req.getCallbackOutOfOrder() != null ? req.getCallbackOutOfOrder() : cur.callbackOutOfOrder(),
                req.getSmsFailEnabled() != null ? req.getSmsFailEnabled() : cur.smsFailEnabled(),
                req.getSmsDelayMs() != null ? Math.max(0L, req.getSmsDelayMs()) : cur.smsDelayMs(),
                req.getScmPartialFail() != null ? req.getScmPartialFail() : cur.scmPartialFail()
        ));
    }

    public void applyDelay(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean hitFailRate(double failRate) {
        if (failRate <= 0.0d) {
            return false;
        }
        if (failRate >= 1.0d) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < failRate;
    }

    private String nonBlankOrDefault(String candidate, String fallback) {
        return (candidate != null && !candidate.isBlank()) ? candidate.trim() : fallback;
    }

    private double clampRate(double rate) {
        return Math.max(0.0d, Math.min(1.0d, rate));
    }
}
