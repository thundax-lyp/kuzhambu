package com.thundax.kuzhambu.operations.application.health.support;

import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeProperties;
import com.thundax.kuzhambu.operations.application.health.configure.OperationsExternalHealthProbeProperties.Target;
import com.thundax.kuzhambu.operations.application.health.support.OperationsHealthProbe.OperationsHealthProbeOutcome;
import com.thundax.kuzhambu.operations.domain.health.codec.HealthCheckIdCodec;
import com.thundax.kuzhambu.operations.domain.health.model.entity.HealthCheckRecord;
import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthCheckId;
import com.thundax.kuzhambu.operations.domain.health.repository.HealthCheckRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OperationsHealthCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsHealthCollector.class);

    public static final String HEALTH_STATUS_UP = "UP";
    public static final String HEALTH_STATUS_DEGRADED = "DEGRADED";
    public static final String HEALTH_STATUS_DOWN = "DOWN";

    private static final int MESSAGE_MAX_LENGTH = 1024;
    private static final Set<String> SUPPORTED_STATUSES =
            Set.of(HEALTH_STATUS_UP, HEALTH_STATUS_DEGRADED, HEALTH_STATUS_DOWN);

    private final HealthCheckRepository healthCheckRepository;
    private final OperationsHealthAlertStrategy healthAlertStrategy;
    private final List<OperationsHealthProbe> probes;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public OperationsHealthCollector(
            HealthCheckRepository healthCheckRepository,
            OperationsHealthAlertStrategy healthAlertStrategy,
            List<OperationsHealthProbe> probes,
            OperationsExternalHealthProbeProperties externalProbeProperties) {
        this.healthCheckRepository = healthCheckRepository;
        this.healthAlertStrategy = healthAlertStrategy;
        this.probes = buildProbes(probes, externalProbeProperties);
    }

    public List<HealthCheckId> collect() {
        List<HealthCheckId> healthCheckIds = new ArrayList<>();
        for (OperationsHealthProbe probe : probes) {
            HealthCheckRecord record = buildRecord(probe);
            healthCheckIds.add(healthCheckRepository.insert(record));
            evaluateHealthAlert(record);
        }
        return healthCheckIds;
    }

    private void evaluateHealthAlert(HealthCheckRecord record) {
        try {
            healthAlertStrategy.evaluateAfterCollect(record);
        } catch (RuntimeException exception) {
            LOGGER.warn("Operations health alert evaluation failed for component {}", record.getComponent(), exception);
        }
    }

    private HealthCheckRecord buildRecord(OperationsHealthProbe probe) {
        Instant checkedAt = Instant.now();
        try {
            OperationsHealthProbeOutcome result = probe.probe();
            if (result == null) {
                return failureRecord(probe, "health probe returned null result", checkedAt);
            }
            return new HealthCheckRecord(
                    nextHealthCheckId(),
                    normalizeRequired(probe.component(), "unknown"),
                    normalizeStatus(result.getHealthStatus()),
                    normalizeLatency(result.getLatencyMs()),
                    trimMessage(result.getMessage()),
                    normalizeRequired(probe.probeSource(), "UNKNOWN"),
                    normalizeOptional(probe.probeTarget()),
                    normalizeOptional(result.getDetailsJson()),
                    checkedAt);
        } catch (RuntimeException ex) {
            return failureRecord(probe, ex.getMessage(), checkedAt);
        }
    }

    private HealthCheckRecord failureRecord(OperationsHealthProbe probe, String message, Instant checkedAt) {
        return new HealthCheckRecord(
                nextHealthCheckId(),
                normalizeRequired(probe.component(), "unknown"),
                HEALTH_STATUS_DOWN,
                null,
                trimMessage(StringUtils.defaultIfBlank(message, "health probe failed")),
                normalizeRequired(probe.probeSource(), "UNKNOWN"),
                normalizeOptional(probe.probeTarget()),
                null,
                checkedAt);
    }

    private HealthCheckId nextHealthCheckId() {
        return HealthCheckIdCodec.toDomain(idGenerator.nextId().value());
    }

    private static String normalizeStatus(String healthStatus) {
        String normalized = StringUtils.trimToEmpty(healthStatus).toUpperCase(Locale.ROOT);
        return SUPPORTED_STATUSES.contains(normalized) ? normalized : HEALTH_STATUS_DEGRADED;
    }

    private static Integer normalizeLatency(Integer latencyMs) {
        if (latencyMs == null || latencyMs < 0) {
            return null;
        }
        return latencyMs;
    }

    private static String normalizeRequired(String value, String defaultValue) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? defaultValue : normalized;
    }

    private static String normalizeOptional(String value) {
        return StringUtils.trimToNull(value);
    }

    private static String trimMessage(String message) {
        String normalized = StringUtils.trimToNull(message);
        if (normalized == null || normalized.length() <= MESSAGE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MESSAGE_MAX_LENGTH);
    }

    private static List<OperationsHealthProbe> buildProbes(
            List<OperationsHealthProbe> localProbes, OperationsExternalHealthProbeProperties externalProbeProperties) {
        Stream<OperationsHealthProbe> localProbeStream = localProbes == null ? Stream.empty() : localProbes.stream();
        Stream<OperationsHealthProbe> externalProbeStream = buildExternalProbes(externalProbeProperties).stream();
        return Stream.concat(localProbeStream, externalProbeStream).toList();
    }

    private static List<OperationsHealthProbe> buildExternalProbes(
            OperationsExternalHealthProbeProperties externalProbeProperties) {
        if (externalProbeProperties == null
                || !externalProbeProperties.isEnabled()
                || externalProbeProperties.getTargets() == null) {
            return List.of();
        }
        return externalProbeProperties.getTargets().stream()
                .filter(OperationsHealthCollector::isValidExternalTarget)
                .map(target -> new HttpOperationsHealthProbe(target, externalProbeProperties.getTimeoutMs()))
                .map(OperationsHealthProbe.class::cast)
                .toList();
    }

    private static boolean isValidExternalTarget(Target target) {
        if (target == null || !target.isEnabled()) {
            return false;
        }
        String url = StringUtils.trimToEmpty(target.getUrl()).toLowerCase(Locale.ROOT);
        return StringUtils.isNotBlank(target.getComponent())
                && (url.startsWith("http://") || url.startsWith("https://"));
    }
}
