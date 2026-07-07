package com.thundax.kuzhambu.operations.application.health.support;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthAlertRecord;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthAlertRepository;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class OperationsHealthAlertStrategy {

    public static final String ALERT_TYPE_HEALTH_DOWN = "HEALTH_DOWN";
    public static final String ALERT_TYPE_HEALTH_DEGRADED = "HEALTH_DEGRADED";
    public static final String ALERT_TYPE_HEALTH_STALE = "HEALTH_STALE";
    public static final String ALERT_LEVEL_WARNING = "WARNING";
    public static final String ALERT_LEVEL_CRITICAL = "CRITICAL";
    public static final String ALERT_STATUS_ACTIVE = "ACTIVE";
    public static final String ALERT_STATUS_RECOVERED = "RECOVERED";
    public static final String SOURCE_REF_TYPE_HEALTH_CHECK = "HEALTH_CHECK";

    private static final int MESSAGE_MAX_LENGTH = 1024;

    private final HealthCheckRepository healthCheckRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final OperationsHealthAlertPolicyProperties properties;
    private final OperationsHealthRecoveryLinkFactory recoveryLinkFactory;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public OperationsHealthAlertStrategy(
            HealthCheckRepository healthCheckRepository,
            HealthAlertRepository healthAlertRepository,
            OperationsHealthAlertPolicyProperties properties,
            OperationsHealthRecoveryLinkFactory recoveryLinkFactory) {
        this.healthCheckRepository = healthCheckRepository;
        this.healthAlertRepository = healthAlertRepository;
        this.properties = properties;
        this.recoveryLinkFactory = recoveryLinkFactory;
    }

    public void evaluateAfterCollect(HealthCheckRecord record) {
        if (record == null || StringUtils.isBlank(record.getComponent())) {
            return;
        }
        if (OperationsHealthCollector.HEALTH_STATUS_DOWN.equals(record.getHealthStatus())) {
            upsertHealthAlert(record, ALERT_TYPE_HEALTH_DOWN, ALERT_LEVEL_CRITICAL, "健康探针返回 DOWN", "查看健康明细并处理组件异常");
            return;
        }
        if (OperationsHealthCollector.HEALTH_STATUS_DEGRADED.equals(record.getHealthStatus())
                && isContinuousStatus(
                        record.getComponent(), OperationsHealthCollector.HEALTH_STATUS_DEGRADED, degradedThreshold())) {
            upsertHealthAlert(
                    record, ALERT_TYPE_HEALTH_DEGRADED, ALERT_LEVEL_WARNING, "健康探针连续返回 DEGRADED", "查看健康明细并观察组件延迟或降级原因");
            return;
        }
        if (OperationsHealthCollector.HEALTH_STATUS_UP.equals(record.getHealthStatus())
                && isContinuousStatus(
                        record.getComponent(), OperationsHealthCollector.HEALTH_STATUS_UP, recoveryUpThreshold())) {
            recoverHealthAlerts(record);
        }
    }

    public void evaluateStaleAlerts() {
        Date now = new Date();
        Date staleBefore =
                new Date(now.getTime() - Duration.ofMinutes(staleMinutes()).toMillis());
        for (HealthCheckRecord latestRecord : healthCheckRepository.listLatestByComponent()) {
            if (latestRecord == null
                    || StringUtils.isBlank(latestRecord.getComponent())
                    || latestRecord.getCheckedAt() == null
                    || !latestRecord.getCheckedAt().before(staleBefore)) {
                continue;
            }
            upsertHealthAlert(
                    latestRecord,
                    ALERT_TYPE_HEALTH_STALE,
                    ALERT_LEVEL_WARNING,
                    "健康采集超过 " + staleMinutes() + " 分钟未更新",
                    "检查健康采集任务和组件探针状态");
        }
    }

    private boolean isContinuousStatus(String component, String healthStatus, int threshold) {
        PageResult<HealthCheckRecord> page = healthCheckRepository.page(component, null, 1, threshold);
        List<HealthCheckRecord> records = page.getRecords();
        return records.size() >= threshold
                && records.stream().allMatch(record -> healthStatus.equals(record.getHealthStatus()));
    }

    private void upsertHealthAlert(
            HealthCheckRecord record, String alertType, String alertLevel, String message, String suggestion) {
        Date now = new Date();
        HealthAlertRecord alert = findOpenHealthAlert(record.getComponent(), alertType);
        boolean createAlert = alert == null;
        if (alert == null) {
            alert = new HealthAlertRecord();
            alert.setId(nextHealthAlertId());
            alert.setFirstTriggeredAt(now);
        }
        alert.setComponent(record.getComponent());
        alert.setAlertType(alertType);
        alert.setAlertLevel(alertLevel);
        alert.setAlertStatus(ALERT_STATUS_ACTIVE);
        alert.setSourceRefType(SOURCE_REF_TYPE_HEALTH_CHECK);
        alert.setSourceRefId(
                ALERT_TYPE_HEALTH_STALE.equals(alertType)
                        ? null
                        : record.getId().value());
        alert.setLatestCheckId(record.getId());
        alert.setMessage(trimMessage(message + "：" + record.getComponent()));
        alert.setSuggestion(suggestion);
        alert.setRecoveryAction(OperationsHealthRecoveryLinkFactory.ACTION_OPEN_HEALTH_DETAIL);
        alert.setRecoveryTarget(recoveryLinkFactory.healthDetailTarget(record.getComponent()));
        alert.setLastTriggeredAt(now);
        alert.setRecoveredAt(null);
        alert.setFailureReason(trimMessage(record.getMessage()));
        if (createAlert) {
            healthAlertRepository.insert(alert);
        } else {
            healthAlertRepository.update(alert);
        }
    }

    private void recoverHealthAlerts(HealthCheckRecord record) {
        for (HealthAlertRecord alert : healthAlertRepository.listOpenByComponent(record.getComponent())) {
            if (!isHealthAlert(alert.getAlertType())) {
                continue;
            }
            alert.setAlertStatus(ALERT_STATUS_RECOVERED);
            alert.setLatestCheckId(record.getId());
            alert.setLastTriggeredAt(record.getCheckedAt());
            alert.setRecoveredAt(record.getCheckedAt());
            alert.setFailureReason(null);
            healthAlertRepository.update(alert);
        }
    }

    private HealthAlertRecord findOpenHealthAlert(String component, String alertType) {
        return healthAlertRepository.listOpenByComponent(component).stream()
                .filter(alert -> alertType.equals(alert.getAlertType()))
                .findFirst()
                .orElse(null);
    }

    private boolean isHealthAlert(String alertType) {
        return ALERT_TYPE_HEALTH_DOWN.equals(alertType)
                || ALERT_TYPE_HEALTH_DEGRADED.equals(alertType)
                || ALERT_TYPE_HEALTH_STALE.equals(alertType);
    }

    private HealthAlertId nextHealthAlertId() {
        return HealthAlertId.of(idGenerator.nextId().value());
    }

    private int degradedThreshold() {
        return Math.max(1, properties.getDegradedThreshold());
    }

    private int recoveryUpThreshold() {
        return Math.max(1, properties.getRecoveryUpThreshold());
    }

    private int staleMinutes() {
        return Math.max(1, properties.getStaleMinutes());
    }

    private static String trimMessage(String message) {
        String normalized = StringUtils.trimToNull(message);
        if (normalized == null || normalized.length() <= MESSAGE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MESSAGE_MAX_LENGTH);
    }
}
