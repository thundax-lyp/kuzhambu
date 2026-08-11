package com.thundax.kuzhambu.operations.application.dashboard.service.impl;

import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryQaTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoverySearchTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryTopQueryFacadeDto;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeMonthlyNewTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeTopTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.AlertSummaryResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.BucketCountResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TaskStatusSummaryResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopAiCapabilityResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopContentResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopQueryResult;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult.TopTagResult;
import com.thundax.kuzhambu.operations.application.dashboard.service.OperationsDashboardApplicationService;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionResolver;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionSnapshot;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryGateway;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import com.thundax.kuzhambu.operations.application.health.result.OperationsHealthSummaryResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class OperationsDashboardApplicationServiceImpl implements OperationsDashboardApplicationService {

    private static final String PERIOD_TYPE_WEEK = "WEEK";
    private static final String PERIOD_TYPE_MONTH = "MONTH";
    private static final String PERIOD_TYPE_CUSTOM = "CUSTOM";
    private static final String HEALTH_STATUS_UP = "UP";
    private static final String ALERT_LEVEL_CRITICAL = "CRITICAL";
    private static final String ALERT_LEVEL_WARNING = "WARNING";
    private static final String TASK_STATUS_RUNNING = "RUNNING";
    private static final String TASK_STATUS_FAILED = "FAILED";
    private static final int TASK_COUNT_PAGE_SIZE = 1;

    private final HealthCheckRepository healthCheckRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final LongTaskSnapshotRepository longTaskSnapshotRepository;
    private final OperationsDashboardSummaryGateway summaryGateway;
    private final OperationsDashboardPermissionResolver permissionResolver;
    private static final OperationsDashboardPermissionSnapshot FULL_PERMISSION_SNAPSHOT =
            new OperationsDashboardPermissionSnapshot(true, true, true, true, true, true, true);

    public OperationsDashboardApplicationServiceImpl(
            HealthCheckRepository healthCheckRepository,
            LongTaskSnapshotRepository longTaskSnapshotRepository,
            OperationsDashboardSummaryGateway summaryGateway) {
        this(healthCheckRepository, null, longTaskSnapshotRepository, summaryGateway, null);
    }

    public OperationsDashboardApplicationServiceImpl(
            HealthCheckRepository healthCheckRepository,
            LongTaskSnapshotRepository longTaskSnapshotRepository,
            OperationsDashboardSummaryGateway summaryGateway,
            OperationsDashboardPermissionResolver permissionResolver) {
        this(healthCheckRepository, null, longTaskSnapshotRepository, summaryGateway, permissionResolver);
    }

    @Autowired
    public OperationsDashboardApplicationServiceImpl(
            HealthCheckRepository healthCheckRepository,
            HealthAlertRepository healthAlertRepository,
            LongTaskSnapshotRepository longTaskSnapshotRepository,
            OperationsDashboardSummaryGateway summaryGateway,
            OperationsDashboardPermissionResolver permissionResolver) {
        this.healthCheckRepository = healthCheckRepository;
        this.healthAlertRepository = healthAlertRepository;
        this.longTaskSnapshotRepository = longTaskSnapshotRepository;
        this.summaryGateway = summaryGateway;
        this.permissionResolver = permissionResolver;
    }

    @Override
    public OperationsDashboardOverviewResult overview(OperationsDashboardOverviewQuery query) {
        PeriodRange periodRange = resolvePeriodRange(query);
        OperationsDashboardPermissionSnapshot permissions = resolvePermissions();
        OperationsCrossDomainSummary summary = summaryGateway.loadSummary(
                periodRange.periodStart(), periodRange.periodEnd(), resolveBucketType(query, periodRange), permissions);
        ClassicsSummaryFacadeResponse classicsSummary =
                permissions.canLoadClassicsSummary() ? summary.classicsSummary() : null;
        AiReportSummaryFacadeResponse aiSummary = permissions.canLoadAiSummary() ? summary.aiSummary() : null;
        DiscoverySummaryFacadeResponse discoverySummary =
                permissions.canLoadDiscoverySummary() ? summary.discoverySummary() : null;
        KnowledgeSummaryFacadeResponse knowledgeSummary =
                permissions.canLoadKnowledgeSummary() ? summary.knowledgeSummary() : null;
        List<OperationsHealthSummaryResult> healthSummaries = permissions.canViewHealthSummary()
                ? healthCheckRepository.listLatestByComponent().stream()
                        .map(this::toHealthSummaryResult)
                        .toList()
                : null;
        List<HealthAlertRecord> openAlerts = permissions.canViewHealthSummary() ? openAlerts() : null;
        Integer runningTaskCount = permissions.canViewTaskSummary() ? countTasks(TASK_STATUS_RUNNING) : null;
        Integer failedTaskCount = permissions.canViewTaskSummary() ? countTasks(TASK_STATUS_FAILED) : null;
        List<TaskStatusSummaryResult> taskStatusSummaries = permissions.canViewTaskSummary()
                ? List.of(
                        new TaskStatusSummaryResult(TASK_STATUS_RUNNING, (long) runningTaskCount),
                        new TaskStatusSummaryResult(TASK_STATUS_FAILED, (long) failedTaskCount))
                : null;
        return new OperationsDashboardOverviewResult(
                periodRange.periodStart(),
                periodRange.periodEnd(),
                permissions.canViewClassicsContentSummary() ? classicsSummary.getContentCount() : null,
                permissions.canViewClassicsContentSummary() ? classicsSummary.getTranslatedContentCount() : null,
                permissions.canViewClassicsContentSummary() ? classicsSummary.getImageReadyContentCount() : null,
                permissions.canViewClassicsContentSummary() ? classicsSummary.getVisualAssetReadyContentCount() : null,
                permissions.canLoadAiSummary() ? aiSummary.getInvocationCount() : null,
                permissions.canLoadAiSummary() ? aiSummary.getSucceededInvocationCount() : null,
                permissions.canLoadAiSummary() ? aiSummary.getFailedInvocationCount() : null,
                permissions.canLoadAiSummary() ? toBigDecimal(aiSummary.getAvgLatencyMs()) : null,
                permissions.canLoadAiSummary() ? aiSummary.getTotalCostAmount() : null,
                permissions.canViewDiscoverySearchSummary() ? discoverySummary.getSearchCount() : null,
                permissions.canViewDiscoveryQaSummary() ? discoverySummary.getQaCount() : null,
                permissions.canViewDiscoverySearchSummary()
                        ? toBigDecimal(discoverySummary.getAvgSearchLatencyMs())
                        : null,
                permissions.canViewKnowledgeTaxonomySummary() ? knowledgeSummary.getTagCoverageRate() : null,
                permissions.canViewHealthSummary() ? unhealthyComponentCount(healthSummaries) : null,
                runningTaskCount,
                failedTaskCount,
                permissions.canViewHealthSummary() ? openAlerts.size() : null,
                permissions.canViewHealthSummary() ? alertLevelCount(openAlerts, ALERT_LEVEL_CRITICAL) : null,
                permissions.canViewHealthSummary() ? alertLevelCount(openAlerts, ALERT_LEVEL_WARNING) : null,
                permissions.canViewHealthSummary() ? highestAlertLevel(openAlerts) : null,
                latestAlert(openAlerts),
                permissions.canViewClassicsContentSummary()
                        ? toContentGrowthSeries(classicsSummary.getContentGrowthSeries())
                        : null,
                permissions.canViewDiscoverySearchSummary()
                        ? toSearchTrendSeries(discoverySummary.getSearchTrendSeries())
                        : null,
                permissions.canViewDiscoveryQaSummary() ? toQaTrendSeries(discoverySummary.getQaTrendSeries()) : null,
                permissions.canViewKnowledgeTaxonomySummary()
                        ? toTagGrowthSeries(knowledgeSummary.getMonthlyNewTags())
                        : null,
                healthSummaries,
                taskStatusSummaries,
                permissions.canViewClassicsContentSummary() ? toTopContents(classicsSummary.getTopContents()) : null,
                permissions.canViewDiscoverySearchSummary() ? toTopQueries(discoverySummary.getTopQueries()) : null,
                permissions.canViewKnowledgeTaxonomySummary() ? toTopTags(knowledgeSummary.getTopTags()) : null,
                permissions.canLoadAiSummary() ? toTopAiCapabilities(aiSummary.getTopCapabilities()) : null);
    }

    private OperationsDashboardPermissionSnapshot resolvePermissions() {
        return permissionResolver == null ? FULL_PERMISSION_SNAPSHOT : permissionResolver.resolve();
    }

    private List<HealthAlertRecord> openAlerts() {
        if (healthAlertRepository == null) {
            return List.of();
        }
        return healthAlertRepository.listOpenSummary();
    }

    private PeriodRange resolvePeriodRange(OperationsDashboardOverviewQuery query) {
        String periodType = normalizePeriodType(query == null ? null : query.periodType());
        if (PERIOD_TYPE_CUSTOM.equals(periodType)) {
            Instant periodStart = query == null ? null : query.periodStart();
            Instant periodEnd = query == null ? null : query.periodEnd();
            if (periodStart == null || periodEnd == null) {
                throw new BizException("Operations dashboard CUSTOM period requires periodStart and periodEnd.");
            }
            if (periodStart.isAfter(periodEnd)) {
                throw new BizException("Operations dashboard periodStart must not be after periodEnd.");
            }
            return new PeriodRange(periodStart, periodEnd);
        }
        Instant periodEnd = Instant.now();
        Instant periodStart =
                switch (periodType) {
                    case PERIOD_TYPE_WEEK -> periodEnd.minus(7, ChronoUnit.DAYS);
                    case PERIOD_TYPE_MONTH -> periodEnd.minus(30, ChronoUnit.DAYS);
                    default -> throw new BizException("Unsupported operations dashboard periodType: " + periodType);
                };
        return new PeriodRange(periodStart, periodEnd);
    }

    private String resolveBucketType(OperationsDashboardOverviewQuery query, PeriodRange periodRange) {
        String periodType = normalizePeriodType(query == null ? null : query.periodType());
        if (PERIOD_TYPE_MONTH.equals(periodType)) {
            return "WEEK";
        }
        if (PERIOD_TYPE_CUSTOM.equals(periodType)) {
            long days = Duration.between(periodRange.periodStart(), periodRange.periodEnd())
                    .toDays();
            return days <= 31 ? "DAY" : "WEEK";
        }
        return "DAY";
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

    private static int alertLevelCount(List<HealthAlertRecord> alerts, String alertLevel) {
        return (int) alerts.stream()
                .filter(alert -> alert != null && alertLevel.equals(alert.getAlertLevel()))
                .count();
    }

    private static String highestAlertLevel(List<HealthAlertRecord> alerts) {
        if (alertLevelCount(alerts, ALERT_LEVEL_CRITICAL) > 0) {
            return ALERT_LEVEL_CRITICAL;
        }
        if (alertLevelCount(alerts, ALERT_LEVEL_WARNING) > 0) {
            return ALERT_LEVEL_WARNING;
        }
        return null;
    }

    private static AlertSummaryResult latestAlert(List<HealthAlertRecord> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            return null;
        }
        HealthAlertRecord alert = alerts.get(0);
        if (alert == null) {
            return null;
        }
        return new AlertSummaryResult(
                alert.getId() == null ? null : alert.getId().value(),
                alert.getComponent(),
                alert.getAlertType(),
                alert.getAlertLevel(),
                alert.getAlertStatus(),
                alert.getSourceRefType(),
                alert.getSourceRefId(),
                alert.getMessage(),
                alert.getSuggestion(),
                alert.getRecoveryAction(),
                alert.getRecoveryTarget(),
                alert.getLastTriggeredAt(),
                alert.getFailureReason());
    }

    private int countTasks(String taskStatus) {
        PageResult<LongTaskSnapshot> taskPage =
                longTaskSnapshotRepository.page(null, null, taskStatus, 1, TASK_COUNT_PAGE_SIZE);
        return (int) taskPage.getTotalCount();
    }

    private static BigDecimal toBigDecimal(Long value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static List<BucketCountResult> toContentGrowthSeries(List<ClassicsContentGrowthPointFacadeDto> points) {
        return safeList(points).stream()
                .map(point -> new BucketCountResult(point.getBucket(), point.getCreatedCount()))
                .toList();
    }

    private static List<BucketCountResult> toSearchTrendSeries(List<DiscoverySearchTrendPointFacadeDto> points) {
        return safeList(points).stream()
                .map(point -> new BucketCountResult(point.getBucket(), point.getSearchCount()))
                .toList();
    }

    private static List<BucketCountResult> toQaTrendSeries(List<DiscoveryQaTrendPointFacadeDto> points) {
        return safeList(points).stream()
                .map(point -> new BucketCountResult(point.getBucket(), point.getQaCount()))
                .toList();
    }

    private static List<BucketCountResult> toTagGrowthSeries(List<KnowledgeMonthlyNewTagFacadeDto> points) {
        return safeList(points).stream()
                .map(point -> new BucketCountResult(point.getBucket(), point.getTagCount()))
                .toList();
    }

    private static List<TopContentResult> toTopContents(List<ClassicsTopContentFacadeDto> topContents) {
        return safeList(topContents).stream()
                .map(content -> new TopContentResult(
                        content.getContentId(), content.getContentType(), content.getTitle(), content.getVisitCount()))
                .toList();
    }

    private static List<TopQueryResult> toTopQueries(List<DiscoveryTopQueryFacadeDto> topQueries) {
        return safeList(topQueries).stream()
                .map(query -> new TopQueryResult(query.getQueryText(), query.getCount()))
                .toList();
    }

    private static List<TopTagResult> toTopTags(List<KnowledgeTopTagFacadeDto> topTags) {
        return safeList(topTags).stream()
                .map(tag -> new TopTagResult(tag.getTagName(), tag.getContentRefCount()))
                .toList();
    }

    private static List<TopAiCapabilityResult> toTopAiCapabilities(List<AiTopCapabilityFacadeDto> topCapabilities) {
        return safeList(topCapabilities).stream()
                .map(capability ->
                        new TopAiCapabilityResult(capability.getCapability(), capability.getInvocationCount()))
                .toList();
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record PeriodRange(Instant periodStart, Instant periodEnd) {}
}
