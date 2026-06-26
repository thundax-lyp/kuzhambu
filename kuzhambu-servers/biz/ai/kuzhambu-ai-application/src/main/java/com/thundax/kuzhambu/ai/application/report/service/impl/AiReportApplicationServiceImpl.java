package com.thundax.kuzhambu.ai.application.report.service.impl;

import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.report.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.application.report.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
import com.thundax.kuzhambu.ai.domain.invocation.repository.AiInvocationRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class AiReportApplicationServiceImpl implements AiReportApplicationService {

    private static final int TOP_CAPABILITY_LIMIT = 10;

    private final AiInvocationRepository aiInvocationRepository;

    public AiReportApplicationServiceImpl(AiInvocationRepository aiInvocationRepository) {
        this.aiInvocationRepository = aiInvocationRepository;
    }

    @Override
    public AiReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {
        List<AiCallRecord> callRecords =
                aiInvocationRepository.listCallRecords(toInstant(periodStart), toInstant(periodEnd));
        long invocationCount = callRecords.size();
        long succeededInvocationCount = callRecords.stream()
                .filter(record -> StringUtils.equalsIgnoreCase(record.getStatus(), "SUCCEEDED"))
                .count();
        long failedInvocationCount = callRecords.stream()
                .filter(record -> StringUtils.equalsIgnoreCase(record.getStatus(), "FAILED"))
                .count();
        long avgLatencyMs = Math.round(callRecords.stream()
                .map(AiCallRecord::getUsage)
                .filter(Objects::nonNull)
                .mapToInt(usage -> usage.getLatencyMs() == null ? 0 : usage.getLatencyMs())
                .average()
                .orElse(0D));
        BigDecimal totalCostAmount = callRecords.stream()
                .map(AiCallRecord::getUsage)
                .filter(Objects::nonNull)
                .map(usage -> usage.getCostAmount() == null ? BigDecimal.ZERO : usage.getCostAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopCapabilityResult> topCapabilities = callRecords.stream()
                .filter(record -> StringUtils.isNotBlank(record.getCapability()))
                .collect(Collectors.groupingBy(AiCallRecord::getCapability, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(TOP_CAPABILITY_LIMIT)
                .map(entry -> new TopCapabilityResult(entry.getKey(), entry.getValue()))
                .toList();

        return new AiReportSummaryResult(
                periodStart,
                periodEnd,
                invocationCount,
                succeededInvocationCount,
                failedInvocationCount,
                avgLatencyMs,
                totalCostAmount,
                topCapabilities);
    }

    private Instant toInstant(Date value) {
        return value == null ? null : value.toInstant();
    }
}
