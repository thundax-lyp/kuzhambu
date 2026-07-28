package com.thundax.kuzhambu.ai.application.invocation.service.impl;

import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult;
import com.thundax.kuzhambu.ai.application.invocation.result.AiReportSummaryResult.TopCapabilityResult;
import com.thundax.kuzhambu.ai.application.invocation.service.AiReportApplicationService;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiInvocationStatus;
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
        List<AiInvocationLog> invocationLogs =
                aiInvocationRepository.listInvocationLogs(toInstant(periodStart), toInstant(periodEnd));
        long invocationCount = invocationLogs.size();
        long succeededInvocationCount = invocationLogs.stream()
                .filter(record -> AiInvocationStatus.SUCCEEDED == record.getStatus())
                .count();
        long failedInvocationCount = invocationLogs.stream()
                .filter(record -> AiInvocationStatus.FAILED == record.getStatus())
                .count();
        long avgLatencyMs = Math.round(invocationLogs.stream()
                .map(AiInvocationLog::getUsage)
                .filter(Objects::nonNull)
                .mapToInt(usage -> usage.getLatencyMs() == null ? 0 : usage.getLatencyMs())
                .average()
                .orElse(0D));
        BigDecimal totalCostAmount = invocationLogs.stream()
                .map(AiInvocationLog::getUsage)
                .filter(Objects::nonNull)
                .map(usage -> usage.getCostAmount() == null ? BigDecimal.ZERO : usage.getCostAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TopCapabilityResult> topCapabilities = invocationLogs.stream()
                .filter(record -> record.getCapability() != null)
                .collect(Collectors.groupingBy(AiInvocationLog::getCapability, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability, Long>
                                        comparingByValue(Comparator.reverseOrder())
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
