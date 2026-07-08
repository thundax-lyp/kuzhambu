package com.thundax.kuzhambu.operations.application.dashboard.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.security.permission.PermissionAuthorizationService;
import com.thundax.kuzhambu.common.security.permission.PrefixPermissionMatcher;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryQaTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoverySearchTrendPointFacadeDto;
import com.thundax.kuzhambu.discovery.facade.dto.DiscoveryTopQueryFacadeDto;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeMonthlyNewTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeTopTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionResolver;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardPermissionSnapshot;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryGateway;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OperationsDashboardApplicationServiceImplTest {

    @Test
    void overviewShouldResolvePermissionSnapshotToSummaryGateway() {
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                new InMemoryHealthCheckRepository(),
                new InMemoryLongTaskSnapshotRepository(),
                summaryGateway,
                new TestPermissionResolver(permissionSnapshotWithSearchOnly()));

        service.overview(new OperationsDashboardOverviewQuery("WEEK", null, null));

        assertEquals(permissionSnapshotWithSearchOnly(), summaryGateway.permissions);
    }

    @Test
    void overviewShouldResolveDefaultWeekAndMapRealSummaryValues() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords = List.of(healthRecord(9001L, "admin-server", "UP"));
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        taskRepository.totalByStatus.put("RUNNING", 2L);
        taskRepository.totalByStatus.put("FAILED", 1L);
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository, taskRepository, summaryGateway, permissionResolverWithAllPrivileges());

        OperationsDashboardOverviewResult result = service.overview(null);

        assertNotNull(result.getPeriodStart());
        assertNotNull(result.getPeriodEnd());
        assertEquals("DAY", summaryGateway.bucketType);
        assertEquals(12L, result.getContentCount());
        assertEquals(5L, result.getTranslatedContentCount());
        assertEquals(4L, result.getImageReadyContentCount());
        assertEquals(3L, result.getVisualAssetReadyContentCount());
        assertEquals(88L, result.getShareVisitCount());
        assertEquals(20L, result.getAiInvocationCount());
        assertEquals(18L, result.getAiSucceededInvocationCount());
        assertEquals(2L, result.getAiFailedInvocationCount());
        assertEquals(new BigDecimal("230"), result.getAiAvgLatencyMs());
        assertEquals(new BigDecimal("8.88"), result.getAiTotalCostAmount());
        assertEquals(30L, result.getSearchCount());
        assertEquals(7L, result.getQaCount());
        assertEquals(new BigDecimal("150"), result.getAvgSearchLatencyMs());
        assertEquals(new BigDecimal("0.75"), result.getTagCoverageRate());
        assertEquals(0, result.getUnhealthyComponentCount());
        assertEquals(2, result.getRunningTaskCount());
        assertEquals(1, result.getFailedTaskCount());
        assertEquals(1, result.getHealthSummaries().size());
        assertEquals(2, result.getTaskStatusSummaries().size());
        assertEquals("2026-06-01", result.getContentGrowthSeries().get(0).getBucket());
        assertEquals(2L, result.getContentGrowthSeries().get(0).getCount());
        assertEquals("2026-06-01", result.getSearchTrendSeries().get(0).getBucket());
        assertEquals(9L, result.getSearchTrendSeries().get(0).getCount());
        assertEquals("2026-06", result.getTagGrowthSeries().get(0).getBucket());
        assertEquals(6L, result.getTagGrowthSeries().get(0).getCount());
        assertEquals("黄帝", result.getTopQueries().get(0).getQueryText());
        assertEquals("礼制", result.getTopTags().get(0).getTagName());
        assertEquals("TRANSLATE", result.getTopAiCapabilities().get(0).getCapability());
    }

    @Test
    void overviewShouldReturnNullForFieldsWhenNoDashboardPermission() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        taskRepository.totalByStatus.put("RUNNING", 3L);
        taskRepository.totalByStatus.put("FAILED", 2L);
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository,
                taskRepository,
                new InMemorySummaryGateway(summary()),
                new TestPermissionResolver(permissionSnapshotWithNoPrivileges()));

        OperationsDashboardOverviewResult result = service.overview(null);

        assertNotNull(result.getPeriodStart());
        assertNotNull(result.getPeriodEnd());
        assertNull(result.getContentCount());
        assertNull(result.getTranslatedContentCount());
        assertNull(result.getImageReadyContentCount());
        assertNull(result.getVisualAssetReadyContentCount());
        assertNull(result.getShareVisitCount());
        assertNull(result.getAiInvocationCount());
        assertNull(result.getAiSucceededInvocationCount());
        assertNull(result.getAiFailedInvocationCount());
        assertNull(result.getAiAvgLatencyMs());
        assertNull(result.getAiTotalCostAmount());
        assertNull(result.getSearchCount());
        assertNull(result.getQaCount());
        assertNull(result.getAvgSearchLatencyMs());
        assertNull(result.getTagCoverageRate());
        assertNull(result.getUnhealthyComponentCount());
        assertNull(result.getRunningTaskCount());
        assertNull(result.getFailedTaskCount());
        assertNull(result.getActiveAlertCount());
        assertNull(result.getCriticalAlertCount());
        assertNull(result.getWarningAlertCount());
        assertNull(result.getHighestAlertLevel());
        assertNull(result.getLatestAlert());
        assertNull(result.getContentGrowthSeries());
        assertNull(result.getSearchTrendSeries());
        assertNull(result.getQaTrendSeries());
        assertNull(result.getTagGrowthSeries());
        assertNull(result.getHealthSummaries());
        assertNull(result.getTaskStatusSummaries());
        assertNull(result.getTopContents());
        assertNull(result.getTopQueries());
        assertNull(result.getTopTags());
        assertNull(result.getTopAiCapabilities());
        assertEquals(0, healthRepository.listLatestByComponentCallCount);
        assertEquals(0, taskRepository.pageCallCount);
    }

    @Test
    void overviewShouldOnlyShowSearchPermissionFields() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository,
                new InMemoryLongTaskSnapshotRepository(),
                summaryGateway,
                new TestPermissionResolver(permissionSnapshotWithSearchOnly()));

        OperationsDashboardOverviewResult result = service.overview(null);

        assertNotNull(summaryGateway.permissions);
        assertEquals(permissionSnapshotWithSearchOnly(), summaryGateway.permissions);
        assertNull(result.getContentCount());
        assertNull(result.getShareVisitCount());
        assertEquals(30L, result.getSearchCount());
        assertNull(result.getQaCount());
        assertEquals(new BigDecimal("150"), result.getAvgSearchLatencyMs());
        assertNotNull(result.getSearchTrendSeries());
        assertEquals("2026-06-01", result.getSearchTrendSeries().get(0).getBucket());
        assertEquals(9L, result.getSearchTrendSeries().get(0).getCount());
        assertNotNull(result.getTopQueries());
        assertEquals("黄帝", result.getTopQueries().get(0).getQueryText());
        assertNull(result.getContentGrowthSeries());
        assertNull(result.getTopContents());
        assertNull(result.getTagGrowthSeries());
        assertNull(result.getTagCoverageRate());
        assertEquals(0, healthRepository.listLatestByComponentCallCount);
    }

    @Test
    void overviewShouldOnlyShowTaskFields() {
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        taskRepository.totalByStatus.put("RUNNING", 6L);
        taskRepository.totalByStatus.put("FAILED", 1L);
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                new InMemoryHealthCheckRepository(),
                taskRepository,
                new InMemorySummaryGateway(summary()),
                new TestPermissionResolver(permissionSnapshotWithTaskOnly()));

        OperationsDashboardOverviewResult result = service.overview(null);

        assertEquals(6, result.getRunningTaskCount());
        assertEquals(1, result.getFailedTaskCount());
        assertNotNull(result.getTaskStatusSummaries());
        assertEquals("RUNNING", result.getTaskStatusSummaries().get(0).getTaskStatus());
        assertEquals("FAILED", result.getTaskStatusSummaries().get(1).getTaskStatus());
        assertNull(result.getContentCount());
        assertNull(result.getSearchCount());
        assertNull(result.getSearchTrendSeries());
        assertNull(result.getHealthSummaries());
    }

    @Test
    void overviewShouldOnlyShowHealthFields() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords = List.of(healthRecord(9001L, "admin-server", "DEGRADED"));
        InMemoryHealthAlertRepository alertRepository = new InMemoryHealthAlertRepository();
        alertRepository.alertsByAll = List.of(new HealthAlertRecord(
                HealthAlertId.of(1201L),
                "admin-server",
                "HEALTH",
                "CRITICAL",
                "ACTIVE",
                "TASK",
                5601L,
                HealthCheckId.of(9001L),
                "组件健康异常",
                "建议重试",
                "已重启",
                "task",
                null,
                new Date(1_719_630_410_000L),
                null,
                null,
                null,
                "连续失败"));
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository,
                alertRepository,
                taskRepository,
                new InMemorySummaryGateway(summary()),
                new TestPermissionResolver(permissionSnapshotWithHealthOnly()));

        OperationsDashboardOverviewResult result = service.overview(null);

        assertNotNull(result.getHealthSummaries());
        assertEquals(1, result.getHealthSummaries().size());
        assertEquals(1, result.getActiveAlertCount());
        assertEquals(1, result.getCriticalAlertCount());
        assertEquals("CRITICAL", result.getHighestAlertLevel());
        assertEquals(1201L, result.getLatestAlert().getAlertId());
        assertNull(result.getContentCount());
        assertNull(result.getSearchCount());
        assertNull(result.getSearchTrendSeries());
        assertNull(result.getQaTrendSeries());
        assertNull(result.getAiInvocationCount());
        assertNull(result.getTagCoverageRate());
        assertNull(result.getRunningTaskCount());
        assertNull(result.getFailedTaskCount());
        assertEquals(0, taskRepository.pageCallCount);
        assertEquals(1, alertRepository.listOpenSummaryCallCount);
    }

    @Test
    void overviewShouldSupportCustomPeriodAndCountUnhealthyComponents() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords =
                List.of(healthRecord(9001L, "admin-server", "DOWN"), healthRecord(9002L, "worker", "DEGRADED"));
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository,
                new InMemoryLongTaskSnapshotRepository(),
                summaryGateway,
                permissionResolverWithAllPrivileges());

        OperationsDashboardOverviewResult result = service.overview(new OperationsDashboardOverviewQuery(
                "CUSTOM", new Date(1_719_630_400_000L), new Date(1_719_716_800_000L)));

        assertEquals(new Date(1_719_630_400_000L), result.getPeriodStart());
        assertEquals(new Date(1_719_716_800_000L), result.getPeriodEnd());
        assertEquals("DAY", summaryGateway.bucketType);
        assertEquals(2, result.getUnhealthyComponentCount());
    }

    @Test
    void overviewShouldResolveMonthAndLongCustomPeriodBuckets() {
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                new InMemoryHealthCheckRepository(),
                new InMemoryLongTaskSnapshotRepository(),
                summaryGateway,
                permissionResolverWithAllPrivileges());

        service.overview(new OperationsDashboardOverviewQuery("MONTH", null, null));
        assertEquals("WEEK", summaryGateway.bucketType);

        service.overview(new OperationsDashboardOverviewQuery(
                "CUSTOM", new Date(1_717_286_400_000L), new Date(1_720_483_200_000L)));
        assertEquals("WEEK", summaryGateway.bucketType);
    }

    @Test
    void overviewShouldRejectInvalidCustomPeriod() {
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                new InMemoryHealthCheckRepository(),
                new InMemoryLongTaskSnapshotRepository(),
                new InMemorySummaryGateway(summary()),
                permissionResolverWithAllPrivileges());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.overview(new OperationsDashboardOverviewQuery(
                        "CUSTOM", new Date(1_719_716_800_000L), new Date(1_719_630_400_000L))));
    }

    private static OperationsCrossDomainSummary summary() {
        return new OperationsCrossDomainSummary(
                ClassicsSummaryFacadeResponse.builder()
                        .contentCount(12L)
                        .translatedContentCount(5L)
                        .imageReadyContentCount(4L)
                        .visualAssetReadyContentCount(3L)
                        .shareVisitCount(88L)
                        .contentGrowthSeries(List.of(ClassicsContentGrowthPointFacadeDto.builder()
                                .bucket("2026-06-01")
                                .createdCount(2L)
                                .build()))
                        .topContents(List.of(ClassicsTopContentFacadeDto.builder()
                                .contentId(1001L)
                                .contentType("SANCAI_ENTRY")
                                .title("黄帝")
                                .visitCount(10L)
                                .build()))
                        .build(),
                AiReportSummaryFacadeResponse.builder()
                        .invocationCount(20L)
                        .succeededInvocationCount(18L)
                        .failedInvocationCount(2L)
                        .avgLatencyMs(230L)
                        .totalCostAmount(new BigDecimal("8.88"))
                        .topCapabilities(List.of(AiTopCapabilityFacadeDto.builder()
                                .capability("TRANSLATE")
                                .invocationCount(11L)
                                .build()))
                        .build(),
                DiscoverySummaryFacadeResponse.builder()
                        .searchCount(30L)
                        .qaCount(7L)
                        .avgSearchLatencyMs(150L)
                        .searchTrendSeries(List.of(DiscoverySearchTrendPointFacadeDto.builder()
                                .bucket("2026-06-01")
                                .searchCount(9L)
                                .build()))
                        .qaTrendSeries(List.of(DiscoveryQaTrendPointFacadeDto.builder()
                                .bucket("2026-06-01")
                                .qaCount(3L)
                                .build()))
                        .topQueries(List.of(DiscoveryTopQueryFacadeDto.builder()
                                .queryText("黄帝")
                                .count(8L)
                                .build()))
                        .build(),
                KnowledgeSummaryFacadeResponse.builder()
                        .tagCoverageRate(new BigDecimal("0.75"))
                        .monthlyNewTags(List.of(KnowledgeMonthlyNewTagFacadeDto.builder()
                                .bucket("2026-06")
                                .tagCount(6L)
                                .build()))
                        .topTags(List.of(KnowledgeTopTagFacadeDto.builder()
                                .tagName("礼制")
                                .contentRefCount(13L)
                                .build()))
                        .build());
    }

    private static HealthCheckRecord healthRecord(long checkId, String component, String healthStatus) {
        return new HealthCheckRecord(
                HealthCheckId.of(checkId),
                component,
                healthStatus,
                12,
                "ok",
                "LOCAL",
                component,
                null,
                new Date(1_719_630_400_000L));
    }

    private static OperationsDashboardPermissionResolver permissionResolverWithAllPrivileges() {
        return new TestPermissionResolver(permissionSnapshotWithAllPrivileges());
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithAllPrivileges() {
        return new OperationsDashboardPermissionSnapshot(true, true, true, true, true, true, true, true);
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithNoPrivileges() {
        return new OperationsDashboardPermissionSnapshot(false, false, false, false, false, false, false, false);
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithSearchOnly() {
        return new OperationsDashboardPermissionSnapshot(false, false, true, false, false, false, false, false);
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithTaskOnly() {
        return new OperationsDashboardPermissionSnapshot(false, false, false, false, false, false, false, true);
    }

    private static OperationsDashboardPermissionSnapshot permissionSnapshotWithHealthOnly() {
        return new OperationsDashboardPermissionSnapshot(false, false, false, false, false, false, true, false);
    }

    private static final class InMemoryHealthAlertRepository implements HealthAlertRepository {
        private List<HealthAlertRecord> alertsByAll = List.of();
        private int listOpenSummaryCallCount;

        @Override
        public HealthAlertRecord getById(HealthAlertId id) {
            return null;
        }

        @Override
        public HealthAlertRecord getOpenBySource(String sourceRefType, Long sourceRefId, String alertType) {
            return null;
        }

        @Override
        public PageResult<HealthAlertRecord> page(
                String component,
                String alertLevel,
                String alertStatus,
                String sourceRefType,
                Long sourceRefId,
                Long latestCheckId,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public List<HealthAlertRecord> listOpenByComponent(String component) {
            return alertsByAll;
        }

        @Override
        public List<HealthAlertRecord> listOpenSummary() {
            listOpenSummaryCallCount++;
            return alertsByAll;
        }

        @Override
        public HealthAlertId insert(HealthAlertRecord record) {
            return record.getId();
        }

        @Override
        public int update(HealthAlertRecord record) {
            return 0;
        }
    }

    private static final class TestPermissionResolver extends OperationsDashboardPermissionResolver {
        private final OperationsDashboardPermissionSnapshot snapshot;

        private TestPermissionResolver(OperationsDashboardPermissionSnapshot snapshot) {
            super(new PermissionAuthorizationService(new PrefixPermissionMatcher()));
            this.snapshot = snapshot;
        }

        @Override
        public OperationsDashboardPermissionSnapshot resolve() {
            return snapshot;
        }
    }

    private static final class InMemoryHealthCheckRepository implements HealthCheckRepository {
        private List<HealthCheckRecord> latestRecords = List.of();
        private int listLatestByComponentCallCount;

        @Override
        public HealthCheckRecord getById(HealthCheckId id) {
            return null;
        }

        @Override
        public List<HealthCheckRecord> listLatestByComponent() {
            listLatestByComponentCallCount++;
            return latestRecords;
        }

        @Override
        public PageResult<HealthCheckRecord> page(
                String component,
                String healthStatus,
                String probeSource,
                String probeTarget,
                Date checkedAtStart,
                Date checkedAtEnd,
                int pageNo,
                int pageSize) {
            return PageResult.of(pageNo, pageSize, 0, List.of());
        }

        @Override
        public List<HealthTrendBucket> listTrend(
                String component, String probeSource, Date periodStart, Date periodEnd, String bucketType) {
            return List.of();
        }

        @Override
        public HealthCheckId insert(HealthCheckRecord record) {
            return record.getId();
        }

        @Override
        public int update(HealthCheckRecord record) {
            return 0;
        }

        @Override
        public int deleteById(HealthCheckId id) {
            return 0;
        }
    }

    private static final class InMemoryLongTaskSnapshotRepository implements LongTaskSnapshotRepository {
        private final Map<String, Long> totalByStatus = new LinkedHashMap<>();
        private int pageCallCount;

        @Override
        public LongTaskSnapshot getById(LongTaskSnapshotId id) {
            return null;
        }

        @Override
        public PageResult<LongTaskSnapshot> page(
                String sourceDomain, String taskType, String taskStatus, int pageNo, int pageSize) {
            pageCallCount++;
            return PageResult.of(pageNo, pageSize, totalByStatus.getOrDefault(taskStatus, 0L), List.of());
        }

        @Override
        public LongTaskSnapshotId insert(LongTaskSnapshot snapshot) {
            return snapshot.getId();
        }

        @Override
        public int update(LongTaskSnapshot snapshot) {
            return 0;
        }

        @Override
        public int deleteById(LongTaskSnapshotId id) {
            return 0;
        }
    }

    private static final class InMemorySummaryGateway implements OperationsDashboardSummaryGateway {
        private final OperationsCrossDomainSummary summary;
        private String bucketType;
        private OperationsDashboardPermissionSnapshot permissions;

        private InMemorySummaryGateway(OperationsCrossDomainSummary summary) {
            this.summary = summary;
        }

        @Override
        public OperationsCrossDomainSummary loadSummary(
                Date periodStart,
                Date periodEnd,
                String bucketType,
                OperationsDashboardPermissionSnapshot permissions) {
            this.bucketType = bucketType;
            this.permissions = permissions;
            return summary;
        }
    }
}
