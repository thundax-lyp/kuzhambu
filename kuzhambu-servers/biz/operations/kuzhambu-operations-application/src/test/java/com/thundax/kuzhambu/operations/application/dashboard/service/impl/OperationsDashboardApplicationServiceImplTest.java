package com.thundax.kuzhambu.operations.application.dashboard.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.dashboard.query.OperationsDashboardOverviewQuery;
import com.thundax.kuzhambu.operations.application.dashboard.result.OperationsDashboardOverviewResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import com.thundax.kuzhambu.operations.domain.task.model.entity.LongTaskSnapshot;
import com.thundax.kuzhambu.operations.domain.task.model.valueobject.LongTaskSnapshotId;
import com.thundax.kuzhambu.operations.domain.task.repository.LongTaskSnapshotRepository;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OperationsDashboardApplicationServiceImplTest {

    @Test
    void overviewShouldResolveDefaultWeekAndReturnStableZeroValues() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords = List.of(healthRecord(9001L, "admin-server", "UP"));
        InMemoryLongTaskSnapshotRepository taskRepository = new InMemoryLongTaskSnapshotRepository();
        taskRepository.totalByStatus.put("RUNNING", 2L);
        taskRepository.totalByStatus.put("FAILED", 1L);
        OperationsDashboardApplicationServiceImpl service =
                new OperationsDashboardApplicationServiceImpl(healthRepository, taskRepository);

        OperationsDashboardOverviewResult result = service.overview(null);

        assertNotNull(result.getPeriodStart());
        assertNotNull(result.getPeriodEnd());
        assertEquals(0L, result.getContentCount());
        assertEquals(0L, result.getAiInvocationCount());
        assertEquals(0L, result.getSearchCount());
        assertEquals(0, result.getUnhealthyComponentCount());
        assertEquals(2, result.getRunningTaskCount());
        assertEquals(1, result.getFailedTaskCount());
        assertEquals(1, result.getHealthSummaries().size());
        assertEquals(2, result.getTaskStatusSummaries().size());
        assertEquals(List.of(), result.getContentGrowthSeries());
        assertEquals(List.of(), result.getTopQueries());
    }

    @Test
    void overviewShouldSupportCustomPeriodAndCountUnhealthyComponents() {
        InMemoryHealthCheckRepository healthRepository = new InMemoryHealthCheckRepository();
        healthRepository.latestRecords =
                List.of(healthRecord(9001L, "admin-server", "DOWN"), healthRecord(9002L, "worker", "DEGRADED"));
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                healthRepository, new InMemoryLongTaskSnapshotRepository());

        OperationsDashboardOverviewResult result = service.overview(new OperationsDashboardOverviewQuery(
                "CUSTOM", new Date(1_719_630_400_000L), new Date(1_719_716_800_000L)));

        assertEquals(new Date(1_719_630_400_000L), result.getPeriodStart());
        assertEquals(new Date(1_719_716_800_000L), result.getPeriodEnd());
        assertEquals(2, result.getUnhealthyComponentCount());
    }

    @Test
    void overviewShouldRejectInvalidCustomPeriod() {
        OperationsDashboardApplicationServiceImpl service = new OperationsDashboardApplicationServiceImpl(
                new InMemoryHealthCheckRepository(), new InMemoryLongTaskSnapshotRepository());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.overview(new OperationsDashboardOverviewQuery(
                        "CUSTOM", new Date(1_719_716_800_000L), new Date(1_719_630_400_000L))));
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
        public PageResult<HealthCheckRecord> page(String component, String healthStatus, int pageNo, int pageSize) {
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
}
