package com.bop.mock.lander.service;

import com.bop.mock.chaos.ChaosConfig;
import com.bop.mock.chaos.ChaosConfigService;
import com.bop.mock.common.BizException;
import com.bop.mock.common.TraceIdFilter;
import com.bop.mock.lander.model.CallbackLogEntry;
import com.bop.mock.lander.model.CallbackPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LanderCallbackService {

    private static final Logger log = LoggerFactory.getLogger(LanderCallbackService.class);
    private static final String SIGNATURE_HEADER = "X-Lander-Signature";
    private static final int MAX_LOGS = 200;
    private static final long OUT_OF_ORDER_OFFSET_MS = 10_000L;

    private final RestClient callbackRestClient;
    private final ObjectMapper objectMapper;
    private final ChaosConfigService chaosConfigService;
    private final String hmacSecret;
    private final ExecutorService callbackExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, Long> lastModifiedByPiid = new ConcurrentHashMap<>();
    private final Deque<CallbackLogEntry> callbackLogs = new ArrayDeque<>(MAX_LOGS);
    private final Map<String, CallbackLogEntry> logIndex = new ConcurrentHashMap<>();

    public LanderCallbackService(
            RestClient callbackRestClient,
            ObjectMapper objectMapper,
            ChaosConfigService chaosConfigService,
            @Value("${mock.lander.callback.hmac-secret:bop-lander-callback-secret}") String hmacSecret) {
        this.callbackRestClient = callbackRestClient;
        this.objectMapper = objectMapper;
        this.chaosConfigService = chaosConfigService;
        this.hmacSecret = hmacSecret;
    }

    @PreDestroy
    public void shutdown() {
        callbackExecutor.close();
    }

    public void dispatchAsync(CallbackPayload payload) {
        ChaosConfig config = chaosConfigService.get();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        callbackExecutor.execute(() -> {
            if (traceId != null) {
                MDC.put(TraceIdFilter.TRACE_ID_KEY, traceId);
            }
            try {
                executeDispatch(payload, config, traceId);
            } finally {
                MDC.remove(TraceIdFilter.TRACE_ID_KEY);
            }
        });
    }

    public CallbackLogEntry triggerSync(CallbackPayload payload) {
        ChaosConfig config = chaosConfigService.get();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return executeDispatch(payload, config, traceId);
    }

    public CallbackLogEntry replay(String logId) {
        CallbackLogEntry origin = logIndex.get(logId);
        if (origin == null) {
            throw new BizException(404, "Callback log not found: " + logId);
        }
        String targetUrl = chaosConfigService.get().landerCallbackUrl();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return sendRaw(origin.piid(), origin.applyId(), origin.modifiedTime(), origin.payloadJson(), targetUrl, traceId);
    }

    public List<CallbackLogEntry> listLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LOGS));
        synchronized (callbackLogs) {
            List<CallbackLogEntry> list = new ArrayList<>(Math.min(safeLimit, callbackLogs.size()));
            for (CallbackLogEntry entry : callbackLogs) {
                list.add(entry);
                if (list.size() >= safeLimit) {
                    break;
                }
            }
            return list;
        }
    }

    private CallbackLogEntry executeDispatch(CallbackPayload payload, ChaosConfig config, String traceId) {
        String targetUrl = config.landerCallbackUrl();
        int repeat = Math.max(1, config.callbackDuplicateCount());
        CallbackLogEntry lastEntry = null;
        for (int i = 0; i < repeat; i++) {
            lastEntry = sendPayload(payload, targetUrl, traceId);
        }

        long latestSent = lastModifiedByPiid.merge(payload.piid(), payload.modifiedTime(), Math::max);
        if (config.callbackOutOfOrder()) {
            // 显式推送早于最近一次回调的时间戳，触发下游乱序拦截验证
            long staleTimestamp = Math.min(payload.modifiedTime(), latestSent) - OUT_OF_ORDER_OFFSET_MS;
            CallbackPayload stalePayload = payload.withModifiedTime(staleTimestamp);
            lastEntry = sendPayload(stalePayload, targetUrl, traceId);
        }
        return lastEntry;
    }

    private CallbackLogEntry sendPayload(CallbackPayload payload, String targetUrl, String traceId) {
        String json = toJson(payload);
        return sendRaw(payload.piid(), payload.applyId(), payload.modifiedTime(), json, targetUrl, traceId);
    }

    private CallbackLogEntry sendRaw(
            String piid, String applyId, long modifiedTime, String payloadJson, String targetUrl, String traceId) {
        long start = System.currentTimeMillis();
        String signature = signHmacSha256(payloadJson);
        int status = 0;
        boolean success = false;
        String responseBody = null;
        String errorMessage = null;

        try {
            RestClient.RequestBodySpec req = callbackRestClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(SIGNATURE_HEADER, signature);
            if (traceId != null && !traceId.isBlank()) {
                req = req.header(TraceIdFilter.TRACE_ID_KEY, traceId);
            }
            ResponseEntity<String> resp = req.body(payloadJson).retrieve().toEntity(String.class);
            status = resp.getStatusCode().value();
            responseBody = resp.getBody();
            success = resp.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException ex) {
            status = ex.getStatusCode().value();
            responseBody = ex.getResponseBodyAsString();
            errorMessage = ex.getMessage();
            log.warn("Lander callback rejected, piid={}, url={}, status={}", piid, targetUrl, status);
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            log.warn("Lander callback failed, piid={}, url={}, err={}", piid, targetUrl, ex.getMessage());
        }

        long durationMs = System.currentTimeMillis() - start;
        CallbackLogEntry entry = new CallbackLogEntry(
                UUID.randomUUID().toString(),
                piid,
                applyId,
                targetUrl,
                modifiedTime,
                status,
                success,
                durationMs,
                responseBody,
                errorMessage,
                traceId,
                payloadJson,
                LocalDateTime.now()
        );
        recordLog(entry);
        return entry;
    }

    private void recordLog(CallbackLogEntry entry) {
        synchronized (callbackLogs) {
            if (callbackLogs.size() >= MAX_LOGS) {
                CallbackLogEntry evicted = callbackLogs.removeLast();
                logIndex.remove(evicted.logId());
            }
            callbackLogs.addFirst(entry);
            logIndex.put(entry.logId(), entry);
        }
    }

    private String toJson(CallbackPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize callback payload", ex);
        }
    }

    private String signHmacSha256(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to compute HMAC-SHA256 signature", ex);
        }
    }
}
