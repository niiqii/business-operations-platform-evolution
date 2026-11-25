package com.bop.mock.notify;

import com.bop.mock.chaos.ChaosConfig;
import com.bop.mock.chaos.ChaosConfigService;
import com.bop.mock.common.ChaosFaultException;
import com.bop.mock.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

@Service
public class NotifyMockService {

    private static final int MAX_RECORDS = 500;

    private final ChaosConfigService chaosConfigService;
    private final Deque<NotifyRecord> ringBuffer = new ArrayDeque<>(MAX_RECORDS);

    public NotifyMockService(ChaosConfigService chaosConfigService) {
        this.chaosConfigService = chaosConfigService;
    }

    public NotifyRecord send(NotifySendRequest request) {
        ChaosConfig config = chaosConfigService.get();
        chaosConfigService.applyDelay(config.smsDelayMs());

        String id = UUID.randomUUID().toString();
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        if (config.smsFailEnabled()) {
            NotifyRecord failed = new NotifyRecord(
                    id,
                    request.getChannel(),
                    request.getRecipient(),
                    request.getTitle(),
                    request.getContent(),
                    request.getBizId(),
                    request.getTemplateCode(),
                    "FAILED",
                    "Simulated notify channel failure",
                    traceId,
                    LocalDateTime.now()
            );
            append(failed);
            throw new ChaosFaultException("Simulated notify channel failure");
        }

        NotifyRecord record = new NotifyRecord(
                id,
                request.getChannel(),
                request.getRecipient(),
                request.getTitle(),
                request.getContent(),
                request.getBizId(),
                request.getTemplateCode(),
                "SUCCESS",
                null,
                traceId,
                LocalDateTime.now()
        );
        append(record);
        return record;
    }

    public List<NotifyRecord> listRecent(String channel, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_RECORDS));
        synchronized (ringBuffer) {
            List<NotifyRecord> result = new ArrayList<>(Math.min(safeLimit, ringBuffer.size()));
            for (NotifyRecord record : ringBuffer) {
                if (channel != null && !channel.isBlank() && !channel.equalsIgnoreCase(record.channel())) {
                    continue;
                }
                result.add(record);
                if (result.size() >= safeLimit) {
                    break;
                }
            }
            return result;
        }
    }

    public void clear() {
        synchronized (ringBuffer) {
            ringBuffer.clear();
        }
    }

    private void append(NotifyRecord record) {
        synchronized (ringBuffer) {
            if (ringBuffer.size() >= MAX_RECORDS) {
                ringBuffer.removeLast();
            }
            ringBuffer.addFirst(record);
        }
    }
}
