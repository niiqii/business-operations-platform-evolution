package com.bop.mock.scm.service;

import com.bop.mock.common.PageResult;
import com.bop.mock.scm.model.ScmContractRecord;
import com.bop.mock.scm.model.ScmVendorRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScmDatasetGenerator {

    private static final int TOTAL_RECORDS = 10_000;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
    private static final long STEP_MINUTES = 60L;
    private static final long STEP_SECONDS = STEP_MINUTES * 60L;
    private static final String[] REGIONS = {"NORTH", "EAST", "SOUTH", "WEST", "CENTRAL"};

    public PageResult<ScmContractRecord> queryContracts(
            LocalDateTime lastUpdateFrom, LocalDateTime lastUpdateTo, int pageNo, int pageSize) {
        IndexRange range = computeIndexRange(lastUpdateFrom, lastUpdateTo);
        return slice(range, pageNo, pageSize, this::buildContract);
    }

    public PageResult<ScmVendorRecord> queryVendors(
            LocalDateTime lastUpdateFrom, LocalDateTime lastUpdateTo, int pageNo, int pageSize) {
        IndexRange range = computeIndexRange(lastUpdateFrom, lastUpdateTo);
        return slice(range, pageNo, pageSize, this::buildVendor);
    }

    // 记录按更新时间单调递增分布，按秒精确 O(1) 定位闭区间并按页生成
    private IndexRange computeIndexRange(LocalDateTime from, LocalDateTime to) {
        int start = 0;
        int end = TOTAL_RECORDS - 1;
        if (from != null && from.isAfter(BASE_TIME)) {
            Duration diff = Duration.between(BASE_TIME, from);
            long ceilSeconds = diff.getNano() > 0 ? diff.getSeconds() + 1 : diff.getSeconds();
            long idx = (ceilSeconds + STEP_SECONDS - 1) / STEP_SECONDS;
            if (idx >= TOTAL_RECORDS) {
                return new IndexRange(1, 0);
            }
            start = (int) idx;
        }
        if (to != null) {
            if (to.isBefore(BASE_TIME)) {
                return new IndexRange(1, 0);
            }
            long idx = Duration.between(BASE_TIME, to).getSeconds() / STEP_SECONDS;
            end = (int) Math.min(TOTAL_RECORDS - 1, idx);
        }
        return new IndexRange(start, end);
    }

    private <T> PageResult<T> slice(IndexRange range, int pageNo, int pageSize, java.util.function.IntFunction<T> builder) {
        int safePageNo = Math.max(1, pageNo);
        int safePageSize = Math.max(1, Math.min(pageSize, 500));
        if (range.start > range.end) {
            return new PageResult<>(safePageNo, safePageSize, 0L, List.of());
        }
        long total = (long) range.end - range.start + 1;
        long offset = (long) (safePageNo - 1) * safePageSize;
        if (offset >= total) {
            return new PageResult<>(safePageNo, safePageSize, total, List.of());
        }
        int sliceStart = (int) (range.start + offset);
        int sliceEnd = (int) Math.min(range.end, sliceStart + safePageSize - 1);
        List<T> records = new ArrayList<>(sliceEnd - sliceStart + 1);
        for (int i = sliceStart; i <= sliceEnd; i++) {
            records.add(builder.apply(i));
        }
        return new PageResult<>(safePageNo, safePageSize, total, records);
    }

    private ScmContractRecord buildContract(int idx) {
        int seq = idx + 1;
        String contractCode = String.format("CON-2025-%05d", seq);
        String vendorCode = String.format("VND-%05d", (idx % TOTAL_RECORDS) + 1);
        BigDecimal contractAmount = BigDecimal.valueOf(100_000L + (seq % 90) * 10_000L).setScale(2, RoundingMode.HALF_UP);
        BigDecimal accountAmount = contractAmount.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
        String frameFlag = (seq % 3 == 0) ? "0" : "1";
        LocalDate startDate = LocalDate.of(2025, 1, 1).plusDays(idx % 180);
        LocalDate endDate = startDate.plusYears(1);
        LocalDateTime updateTime = BASE_TIME.plusMinutes(idx * STEP_MINUTES);
        return new ScmContractRecord(
                contractCode,
                "脱敏服务合同-" + seq,
                vendorCode,
                "脱敏服务商-" + seq,
                frameFlag,
                contractAmount,
                accountAmount,
                startDate,
                endDate,
                "0",
                updateTime
        );
    }

    private ScmVendorRecord buildVendor(int idx) {
        int seq = idx + 1;
        String vendorCode = String.format("VND-%05d", seq);
        String region = REGIONS[idx % REGIONS.length];
        LocalDateTime updateTime = BASE_TIME.plusMinutes(idx * STEP_MINUTES);
        return new ScmVendorRecord(
                vendorCode,
                "脱敏服务商-" + seq,
                "PV-" + String.format("%05d", seq),
                String.format("91000000MOCK%06d", seq),
                region,
                "ACTIVE",
                updateTime
        );
    }

    private record IndexRange(int start, int end) {
    }
}
