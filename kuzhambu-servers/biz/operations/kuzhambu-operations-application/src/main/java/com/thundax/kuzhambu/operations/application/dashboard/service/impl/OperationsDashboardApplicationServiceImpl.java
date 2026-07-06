package com.thundax.kuzhambu.operations.application.dashboard.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TaskStatusSummaryResult;
import com.thundax.kuzhambu.operations.application.dashboard.service.OperationsDashboardApplicationService;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class OperationsDashboardApplicationServiceImpl implements OperationsDashboardApplicationService {

    private static final String PERIOD_TYPE_WEEK = "WEEK";
    private static final String PERIOD_TYPE_MONTH = "MONTH";
    private static final String PERIOD_TYPE_CUSTOM = "CUSTOM";
    private static final String HEALTH_STATUS_UP = "UP";
    private static final String TASK_STATUS_RUNNING = "RUNNING";
    private static final String TASK_STATUS_FAILED = "FAILED";
    private static final int TASK_COUNT_PAGE_SIZE = 1;

    private final HealthCheckRepository healthCheckRepository;
    private final LongTaskSnapshotRepository longTaskSnapshotRepository;

    public OperationsDashboardApplicationServiceImpl(
            HealthCheckRepository healthCheckRepository, LongTaskSnapshotRepository longTaskSnapshotRepository) {
        this.healthCheckRepository = healthCheckRepository;
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
    }

    @Override
    public OperationsDashboardOverviewResult overview(OperationsDashboardOverviewQuery query) {
        PeriodRange periodRange = resolvePeriodRange(query);
        List<OperationsHealthSummaryResult> healthSummaries = healthCheckRepository.listLatestByComponent().stream()
                .map(this::toHealthSummaryResult)
                .collect(Collectors.toList());
        int runningTaskCount = countTasks(TASK_STATUS_RUNNING);
        int failedTaskCount = countTasks(TASK_STATUS_FAILED);
        return new OperationsDashboardOverviewResult(
                periodRange.periodStart(),
                periodRange.periodEnd(),
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                unhealthyComponentCount(healthSummaries),
                runningTaskCount,
                failedTaskCount,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                healthSummaries,
                List.of(
                        new TaskStatusSummaryResult(TASK_STATUS_RUNNING, (long) runningTaskCount),
                        new TaskStatusSummaryResult(TASK_STATUS_FAILED, (long) failedTaskCount)),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private PeriodRange resolvePeriodRange(OperationsDashboardOverviewQuery query) {
        String periodType = normalizePeriodType(query == null ? null : query.getPeriodType());
        if (PERIOD_TYPE_CUSTOM.equals(periodType)) {
            Date periodStart = query == null ? null : query.getPeriodStart();
            Date periodEnd = query == null ? null : query.getPeriodEnd();
            if (periodStart == null || periodEnd == null) {
                throw new IllegalArgumentException(
                        "Operations dashboard CUSTOM period requires periodStart and periodEnd.");
            }
            if (periodStart.after(periodEnd)) {
                throw new IllegalArgumentException("Operations dashboard periodStart must not be after periodEnd.");
            }
            return new PeriodRange(periodStart, periodEnd);
        }
        Instant periodEnd = Instant.now();
        Instant periodStart =
                switch (periodType) {
                    case PERIOD_TYPE_WEEK -> periodEnd.minus(7, ChronoUnit.DAYS);
                    case PERIOD_TYPE_MONTH -> periodEnd.minus(30, ChronoUnit.DAYS);
                    default ->
                        throw new IllegalArgumentException(
                                "Unsupported operations dashboard periodType: " + periodType);
                };
        return new PeriodRange(Date.from(periodStart), Date.from(periodEnd));
    }

    private static String normalizePeriodType(String periodType) {
        if (periodType == null || periodType.isBlank()) {
            return PERIOD_TYPE_WEEK;
        }
        return periodType.trim().toUpperCase(Locale.ROOT);
    }

    private OperationsHealthSummaryResult toHealthSummaryResult(HealthCheckRecord record) {
        if (record == null) {
            return null;
        }
        return new OperationsHealthSummaryResult(
                record.getId(),
                record.getComponent(),
                record.getHealthStatus(),
                record.getLatencyMs(),
                record.getMessage(),
                record.getProbeSource(),
                record.getProbeTarget(),
                record.getCheckedAt());
    }

    private static int unhealthyComponentCount(List<OperationsHealthSummaryResult> healthSummaries) {
        return (int) healthSummaries.stream()
                .filter(summary -> summary != null && !HEALTH_STATUS_UP.equals(summary.getHealthStatus()))
                .count();
    }

    private int countTasks(String taskStatus) {
        PageResult<LongTaskSnapshot> taskPage =
                longTaskSnapshotRepository.page(null, null, taskStatus, 1, TASK_COUNT_PAGE_SIZE);
        return (int) taskPage.getTotalCount();
    }

    private record PeriodRange(Date periodStart, Date periodEnd) {}
}
