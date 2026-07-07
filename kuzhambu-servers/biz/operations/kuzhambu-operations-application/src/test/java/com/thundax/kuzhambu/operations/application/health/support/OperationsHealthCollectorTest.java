package com.thundax.kuzhambu.operations.application.health.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthProbe.OperationsHealthProbeResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsHealthCollectorTest {

    @Test
    void collectShouldPersistSuccessfulProbeRecord() {
        InMemoryHealthCheckRepository repository = new InMemoryHealthCheckRepository();
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        OperationsHealthCollector collector =
                new OperationsHealthCollector(repository, alertStrategy, List.of(successProbe("search", "UP", 12)));

        List<HealthCheckId> healthCheckIds = collector.collect();

        assertEquals(1, healthCheckIds.size());
        HealthCheckRecord record = repository.records.get(0);
        assertNotNull(record.getId());
        assertEquals("search", record.getComponent());
        assertEquals("UP", record.getHealthStatus());
        assertEquals(12, record.getLatencyMs());
        assertEquals("LOCAL", record.getProbeSource());
        assertEquals("search", record.getProbeTarget());
        assertEquals("{\"component\":\"search\"}", record.getDetailsJson());
        assertNotNull(record.getCheckedAt());
        verify(alertStrategy).evaluateAfterCollect(record);
    }

    @Test
    void collectShouldPersistDownRecordWhenProbeFailsAndContinue() {
        InMemoryHealthCheckRepository repository = new InMemoryHealthCheckRepository();
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        OperationsHealthCollector collector = new OperationsHealthCollector(
                repository,
                alertStrategy,
                List.of(failingProbe("ai", "timeout"), successProbe("backup", "BROKEN", -1)));

        collector.collect();

        assertEquals(2, repository.records.size());
        HealthCheckRecord failedRecord = repository.records.get(0);
        HealthCheckRecord degradedRecord = repository.records.get(1);
        assertEquals("ai", failedRecord.getComponent());
        assertEquals("DOWN", failedRecord.getHealthStatus());
        assertEquals("timeout", failedRecord.getMessage());
        assertEquals("backup", degradedRecord.getComponent());
        assertEquals("DEGRADED", degradedRecord.getHealthStatus());
        assertNull(degradedRecord.getLatencyMs());
        verify(alertStrategy).evaluateAfterCollect(failedRecord);
        verify(alertStrategy).evaluateAfterCollect(degradedRecord);
    }

    @Test
    void collectShouldNotRollbackHealthRecordWhenAlertStrategyFails() {
        InMemoryHealthCheckRepository repository = new InMemoryHealthCheckRepository();
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        doThrow(new IllegalStateException("alert failed"))
                .when(alertStrategy)
                .evaluateAfterCollect(org.mockito.Mockito.any());
        OperationsHealthCollector collector =
                new OperationsHealthCollector(repository, alertStrategy, List.of(successProbe("storage", "UP", 3)));

        List<HealthCheckId> healthCheckIds = collector.collect();

        assertEquals(1, healthCheckIds.size());
        assertEquals(1, repository.records.size());
    }

    @Test
    void collectShouldSurfaceRepositoryFailure() {
        InMemoryHealthCheckRepository repository = new InMemoryHealthCheckRepository();
        repository.failInsert = true;
        OperationsHealthAlertStrategy alertStrategy = mock(OperationsHealthAlertStrategy.class);
        OperationsHealthCollector collector =
                new OperationsHealthCollector(repository, alertStrategy, List.of(successProbe("storage", "UP", 3)));

        assertThrows(IllegalStateException.class, collector::collect);
    }

    private static OperationsHealthProbe successProbe(String component, String status, Integer latencyMs) {
        return new OperationsHealthProbe() {
            @Override
            public String component() {
                return component;
            }

            @Override
            public String probeSource() {
                return "LOCAL";
            }

            @Override
            public String probeTarget() {
                return component;
            }

            @Override
            public OperationsHealthProbeResult probe() {
                return new OperationsHealthProbeResult(
                        status, latencyMs, "ok", "{\"component\":\"" + component + "\"}");
            }
        };
    }

    private static OperationsHealthProbe failingProbe(String component, String message) {
        return new OperationsHealthProbe() {
            @Override
            public String component() {
                return component;
            }

            @Override
            public String probeSource() {
                return "LOCAL";
            }

            @Override
            public String probeTarget() {
                return component;
            }

            @Override
            public OperationsHealthProbeResult probe() {
                throw new IllegalStateException(message);
            }
        };
    }

    private static final class InMemoryHealthCheckRepository implements HealthCheckRepository {
        private final List<HealthCheckRecord> records = new ArrayList<>();
        private boolean failInsert;

        @Override
        public HealthCheckRecord getById(HealthCheckId id) {
            return records.stream()
                    .filter(record -> record.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<HealthCheckRecord> listLatestByComponent() {
            return List.copyOf(records);
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
            return PageResult.of(pageNo, pageSize, records.size(), List.copyOf(records));
        }

        @Override
        public List<HealthTrendBucket> listTrend(
                String component, String probeSource, Date periodStart, Date periodEnd, String bucketType) {
            return List.of();
        }

        @Override
        public HealthCheckId insert(HealthCheckRecord record) {
            if (failInsert) {
                throw new IllegalStateException("insert failed");
            }
            records.add(record);
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
}
