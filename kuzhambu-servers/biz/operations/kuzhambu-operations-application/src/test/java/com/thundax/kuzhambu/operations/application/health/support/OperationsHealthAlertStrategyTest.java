package com.thundax.kuzhambu.operations.application.health.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsHealthAlertPolicyProperties;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthAlertIdCodec;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthTrendBucket;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class OperationsHealthAlertStrategyTest {

    @Test
    void evaluateAfterCollectShouldCreateCriticalAlertForDownRecord() {
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        InMemoryHealthAlertRepository healthAlertRepository = new InMemoryHealthAlertRepository();
        OperationsHealthAlertStrategy strategy = strategy(healthCheckRepository, healthAlertRepository);
        HealthCheckRecord record = healthRecord(9101L, "database", OperationsHealthCollector.HEALTH_STATUS_DOWN, now());

        strategy.evaluateAfterCollect(record);

        assertEquals(1, healthAlertRepository.alerts.size());
        HealthAlertRecord alert = healthAlertRepository.alerts.get(0);
        assertEquals("database", alert.getComponent());
        assertEquals(OperationsHealthAlertStrategy.ALERT_TYPE_HEALTH_DOWN, alert.getAlertType());
        assertEquals(OperationsHealthAlertStrategy.ALERT_LEVEL_CRITICAL, alert.getAlertLevel());
        assertEquals(OperationsHealthAlertStrategy.ALERT_STATUS_ACTIVE, alert.getAlertStatus());
        assertEquals("HEALTH_CHECK", alert.getSourceRefType());
        assertEquals(9101L, alert.getSourceRefId());
        assertEquals(9101L, alert.getLatestCheckId().value());
        assertEquals(OperationsHealthRecoveryLinkFactory.ACTION_OPEN_HEALTH_DETAIL, alert.getRecoveryAction());
        assertTrue(alert.getRecoveryTarget().contains("/operations/dashboard"));
    }

    @Test
    void evaluateAfterCollectShouldCreateWarningAlertForContinuousDegradedRecords() {
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        InMemoryHealthAlertRepository healthAlertRepository = new InMemoryHealthAlertRepository();
        OperationsHealthAlertStrategy strategy = strategy(healthCheckRepository, healthAlertRepository);
        Instant baseTime = now();
        HealthCheckRecord latest =
                healthRecord(9103L, "cache", OperationsHealthCollector.HEALTH_STATUS_DEGRADED, baseTime);
        healthCheckRepository.records.add(latest);
        healthCheckRepository.records.add(healthRecord(
                9102L, "cache", OperationsHealthCollector.HEALTH_STATUS_DEGRADED, baseTime.minusMillis(1_000L)));
        healthCheckRepository.records.add(healthRecord(
                9101L, "cache", OperationsHealthCollector.HEALTH_STATUS_DEGRADED, baseTime.minusMillis(2_000L)));

        strategy.evaluateAfterCollect(latest);

        assertEquals(1, healthAlertRepository.alerts.size());
        HealthAlertRecord alert = healthAlertRepository.alerts.get(0);
        assertEquals(OperationsHealthAlertStrategy.ALERT_TYPE_HEALTH_DEGRADED, alert.getAlertType());
        assertEquals(OperationsHealthAlertStrategy.ALERT_LEVEL_WARNING, alert.getAlertLevel());
        assertEquals("OPEN_HEALTH_DETAIL", alert.getRecoveryAction());
        assertTrue(alert.getMessage().contains("连续返回 DEGRADED"));
    }

    @Test
    void evaluateAfterCollectShouldRecoverOpenHealthAlertsForContinuousUpRecords() {
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        InMemoryHealthAlertRepository healthAlertRepository = new InMemoryHealthAlertRepository();
        OperationsHealthAlertStrategy strategy = strategy(healthCheckRepository, healthAlertRepository);
        Instant baseTime = now();
        HealthCheckRecord latest = healthRecord(9202L, "search", OperationsHealthCollector.HEALTH_STATUS_UP, baseTime);
        healthCheckRepository.records.add(latest);
        healthCheckRepository.records.add(healthRecord(
                9201L, "search", OperationsHealthCollector.HEALTH_STATUS_UP, baseTime.minusMillis(1_000L)));
        healthAlertRepository.alerts.add(
                openAlert(9301L, "search", OperationsHealthAlertStrategy.ALERT_TYPE_HEALTH_DOWN, "ACKED"));

        strategy.evaluateAfterCollect(latest);

        HealthAlertRecord alert = healthAlertRepository.alerts.get(0);
        assertEquals(OperationsHealthAlertStrategy.ALERT_STATUS_RECOVERED, alert.getAlertStatus());
        assertEquals(9202L, alert.getLatestCheckId().value());
        assertEquals(baseTime, alert.getRecoveredAt());
        assertNull(alert.getFailureReason());
    }

    @Test
    void evaluateStaleAlertsShouldCreateWarningAlertWithoutSourceRefId() {
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        InMemoryHealthAlertRepository healthAlertRepository = new InMemoryHealthAlertRepository();
        OperationsHealthAlertStrategy strategy = strategy(healthCheckRepository, healthAlertRepository);
        healthCheckRepository.records.add(healthRecord(
                9401L,
                "admin-server",
                OperationsHealthCollector.HEALTH_STATUS_UP,
                now().minus(Duration.ofMinutes(11))));

        strategy.evaluateStaleAlerts();

        assertEquals(1, healthAlertRepository.alerts.size());
        HealthAlertRecord alert = healthAlertRepository.alerts.get(0);
        assertEquals(OperationsHealthAlertStrategy.ALERT_TYPE_HEALTH_STALE, alert.getAlertType());
        assertEquals(OperationsHealthAlertStrategy.ALERT_LEVEL_WARNING, alert.getAlertLevel());
        assertNull(alert.getSourceRefId());
        assertEquals(9401L, alert.getLatestCheckId().value());
        assertTrue(alert.getMessage().contains("10 分钟"));
    }

    @Test
    void recordFailureAlertShouldUseStableFieldsAndUpdateOpenAlert() {
        InMemoryHealthCheckRepository healthCheckRepository = new InMemoryHealthCheckRepository();
        InMemoryHealthAlertRepository healthAlertRepository = new InMemoryHealthAlertRepository();
        OperationsHealthAlertStrategy strategy = strategy(healthCheckRepository, healthAlertRepository);

        strategy.recordBackupFailed(9001L, "script failed");
        strategy.recordBackupFailed(9001L, "script failed again");

        assertEquals(1, healthAlertRepository.alerts.size());
        HealthAlertRecord backupAlert = healthAlertRepository.alerts.get(0);
        assertEquals(OperationsHealthAlertStrategy.ALERT_TYPE_BACKUP_FAILED, backupAlert.getAlertType());
        assertEquals(OperationsHealthAlertStrategy.ALERT_LEVEL_CRITICAL, backupAlert.getAlertLevel());
        assertEquals(OperationsHealthRecoveryLinkFactory.ACTION_RUN_MANUAL_BACKUP, backupAlert.getRecoveryAction());
        assertEquals("script failed again", backupAlert.getFailureReason());
        assertTrue(backupAlert.getRecoveryTarget().contains("manualBackup"));

        strategy.recordCleanupFailed(7001L, "TARGET_NOT_FOUND");
        strategy.recordReportFailed(8001L, "worker boom");
        strategy.recordLongTaskFailed(6001L, "task failed");

        assertEquals(4, healthAlertRepository.alerts.size());
        HealthAlertRecord cleanupAlert = healthAlertRepository.getBySource("CLEANUP", 7001L, "CLEANUP_FAILED");
        HealthAlertRecord reportAlert = healthAlertRepository.getBySource("REPORT", 8001L, "REPORT_FAILED");
        HealthAlertRecord taskAlert = healthAlertRepository.getBySource("LONG_TASK", 6001L, "LONG_TASK_FAILED");
        assertEquals(OperationsHealthAlertStrategy.ALERT_LEVEL_WARNING, cleanupAlert.getAlertLevel());
        assertEquals(OperationsHealthRecoveryLinkFactory.ACTION_OPEN_CLEANUP_DETAIL, cleanupAlert.getRecoveryAction());
        assertEquals(OperationsHealthRecoveryLinkFactory.ACTION_NONE, reportAlert.getRecoveryAction());
        assertNull(reportAlert.getRecoveryTarget());
        assertEquals(OperationsHealthRecoveryLinkFactory.ACTION_OPEN_TASK_DETAIL, taskAlert.getRecoveryAction());
    }

    private static OperationsHealthAlertStrategy strategy(
            HealthCheckRepository healthCheckRepository, HealthAlertRepository healthAlertRepository) {
        OperationsHealthAlertPolicyProperties properties = new OperationsHealthAlertPolicyProperties();
        properties.setDegradedThreshold(3);
        properties.setRecoveryUpThreshold(2);
        properties.setStaleMinutes(10);
        properties.setWriteBlockStaleMinutes(30);
        return new OperationsHealthAlertStrategy(
                healthCheckRepository, healthAlertRepository, properties, new OperationsHealthRecoveryLinkFactory());
    }

    private static HealthCheckRecord healthRecord(
            long checkId, String component, String healthStatus, Instant checkedAt) {
        return new HealthCheckRecord(
                HealthCheckIdCodec.toDomain(checkId),
                component,
                healthStatus,
                12,
                "probe message",
                "LOCAL",
                component,
                "{\"component\":\"" + component + "\"}",
                checkedAt);
    }

    private static HealthAlertRecord openAlert(long alertId, String component, String alertType, String alertStatus) {
        return new HealthAlertRecord(
                HealthAlertIdCodec.toDomain(alertId),
                component,
                alertType,
                "CRITICAL",
                alertStatus,
                "HEALTH_CHECK",
                9001L,
                HealthCheckIdCodec.toDomain(9001L),
                "message",
                "suggestion",
                "OPEN_HEALTH_DETAIL",
                "{\"route\":\"/operations/dashboard\"}",
                now(),
                now(),
                null,
                null,
                null,
                "failure");
    }

    private static Instant now() {
        return Instant.ofEpochMilli(1_719_630_500_000L);
    }

    private static final class InMemoryHealthCheckRepository implements HealthCheckRepository {
        private final List<HealthCheckRecord> records = new ArrayList<>();

        @Override
        public HealthCheckRecord getById(HealthCheckId id) {
            return records.stream()
                    .filter(record -> record.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<HealthCheckRecord> listLatestByComponent() {
            return records.stream().collect(Collectors.groupingBy(HealthCheckRecord::getComponent)).values().stream()
                    .map(componentRecords -> componentRecords.stream()
                            .max(Comparator.comparing(HealthCheckRecord::getCheckedAt))
                            .orElse(null))
                    .toList();
        }

        @Override
        public PageResult<HealthCheckRecord> page(
                String component,
                String healthStatus,
                String probeSource,
                String probeTarget,
                Instant checkedAtStart,
                Instant checkedAtEnd,
                int pageNo,
                int pageSize) {
            List<HealthCheckRecord> matchedRecords = records.stream()
                    .filter(record -> component == null || component.equals(record.getComponent()))
                    .filter(record -> healthStatus == null || healthStatus.equals(record.getHealthStatus()))
                    .sorted(Comparator.comparing(HealthCheckRecord::getCheckedAt)
                            .reversed())
                    .limit(pageSize)
                    .toList();
            return PageResult.of(pageNo, pageSize, matchedRecords.size(), matchedRecords);
        }

        @Override
        public List<HealthTrendBucket> listTrend(
                String component, String probeSource, Instant periodStart, Instant periodEnd, String bucketType) {
            return List.of();
        }

        @Override
        public HealthCheckId insert(HealthCheckRecord record) {
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

    private static final class InMemoryHealthAlertRepository implements HealthAlertRepository {
        private final List<HealthAlertRecord> alerts = new ArrayList<>();

        @Override
        public HealthAlertRecord getById(HealthAlertId id) {
            return alerts.stream()
                    .filter(alert -> alert.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public HealthAlertRecord getBySource(String sourceRefType, Long sourceRefId, String alertType) {
            return alerts.stream()
                    .filter(alert -> sourceRefType.equals(alert.getSourceRefType()))
                    .filter(alert -> sourceRefId == null
                            ? alert.getSourceRefId() == null
                            : sourceRefId.equals(alert.getSourceRefId()))
                    .filter(alert -> alertType.equals(alert.getAlertType()))
                    .filter(OperationsHealthAlertStrategyTest::isOpen)
                    .findFirst()
                    .orElse(null);
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
            return PageResult.of(pageNo, pageSize, alerts.size(), List.copyOf(alerts));
        }

        @Override
        public List<HealthAlertRecord> listOpenByComponent(String component) {
            return alerts.stream()
                    .filter(alert -> component.equals(alert.getComponent()))
                    .filter(OperationsHealthAlertStrategyTest::isOpen)
                    .toList();
        }

        @Override
        public List<HealthAlertRecord> listOpenSummary() {
            return alerts.stream()
                    .filter(OperationsHealthAlertStrategyTest::isOpen)
                    .toList();
        }

        @Override
        public HealthAlertId insert(HealthAlertRecord record) {
            alerts.add(record);
            return record.getId();
        }

        @Override
        public int update(HealthAlertRecord record) {
            return 1;
        }
    }

    private static boolean isOpen(HealthAlertRecord alert) {
        return "ACTIVE".equals(alert.getAlertStatus()) || "ACKED".equals(alert.getAlertStatus());
    }
}
