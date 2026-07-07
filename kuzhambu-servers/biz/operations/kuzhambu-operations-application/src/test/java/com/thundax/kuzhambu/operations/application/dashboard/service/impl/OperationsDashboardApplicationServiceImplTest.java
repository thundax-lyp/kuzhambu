package com.thundax.kuzhambu.operations.application.dashboard.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.ai.facade.dto.AiTopCapabilityFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiReportSummaryFacadeResponse;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
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
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryGateway;
import com.thundax.kuzhambu.operations.application.dashboard.support.OperationsDashboardSummaryModels.OperationsCrossDomainSummary;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
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
    void overviewShouldResolveDefaultWeekAndMapRealSummaryValues() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords = List.of(healthRecord(9001L, "admin-server", "UP"));
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        taskRepository.totalByStatus.put("RUNNING", 2L);
        taskRepository.totalByStatus.put("FAILED", 1L);
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service =
                new OperationsDashboardApplicationServiceImpl(healthRepository, taskRepository, summaryGateway);

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
    void overviewShouldSupportCustomPeriodAndCountUnhealthyComponents() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords =
                List.of(healthRecord(9001L, "admin-server", "DOWN"), healthRecord(9002L, "worker", "DEGRADED"));
        InMemorySummaryGateway summaryGateway = new InMemorySummaryGateway(summary());
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository, new InMemoryLongTaskSnapshotRepository(), summaryGateway);

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
                new InMemoryHealthCheckRepository(), new InMemoryLongTaskSnapshotRepository(), summaryGateway);

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
                new InMemorySummaryGateway(summary()));

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

    private static final class InMemoryHealthCheckRepository implements HealthCheckRepository {
        private List<HealthCheckRecord> latestRecords = List.of();

        @Override
        public HealthCheckRecord getById(HealthCheckId id) {
            return null;
        }

        @Override
        public List<HealthCheckRecord> listLatestByComponent() {
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

        @Override
        public LongTaskSnapshot getById(LongTaskSnapshotId id) {
            return null;
        }

        @Override
        public PageResult<LongTaskSnapshot> page(
                String sourceDomain, String taskType, String taskStatus, int pageNo, int pageSize) {
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

        private InMemorySummaryGateway(OperationsCrossDomainSummary summary) {
            this.summary = summary;
        }

        @Override
        public OperationsCrossDomainSummary loadSummary(Date periodStart, Date periodEnd, String bucketType) {
            this.bucketType = bucketType;
            return summary;
        }
    }
}
