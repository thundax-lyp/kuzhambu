package com.thundax.kuzhambu.operations.application.cleanup.support;

import com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupScheduleProperties;
import java.util.List;

public final class OperationsCleanupPolicies {

    private OperationsCleanupPolicies() {}

    public static List<CleanupPolicy> orderedPolicies(OperationsCleanupScheduleProperties properties) {
        return OperationsCleanupSupport.orderedCleanupTypes().stream()
                .map(cleanupType -> policyFor(properties, cleanupType))
                .toList();
    }

    public static CleanupPolicy policyFor(OperationsCleanupScheduleProperties properties, String cleanupType) {
        String normalizedType = OperationsCleanupSupport.normalizeType(cleanupType);
        OperationsCleanupScheduleProperties.CleanupPolicyProperties policy =
                switch (normalizedType) {
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP ->
                        properties.getPolicies().getExpiredBackup();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT ->
                        properties.getPolicies().getExpiredExport();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_SHARE ->
                        properties.getPolicies().getExpiredShare();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT ->
                        properties.getPolicies().getExpiredDraft();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT ->
                        properties.getPolicies().getExpiredReport();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK ->
                        properties.getPolicies().getExpiredHealthCheck();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK ->
                        properties.getPolicies().getExpiredLongTask();
                    default -> throw new IllegalStateException("Unsupported operations cleanup type: " + cleanupType);
                };
        return new CleanupPolicy(
                normalizedType,
                policy.isEnabled(),
                policy.getRetentionDays(),
                effectiveLimit(properties.getDefaultLimit(), policy.getLimit()));
    }

    private static int effectiveLimit(int defaultLimit, Integer policyLimit) {
        if (policyLimit == null || policyLimit <= 0) {
            return defaultLimit;
        }
        return policyLimit;
    }

    public record CleanupPolicy(String cleanupType, boolean enabled, int retentionDays, int limit) {}
}
