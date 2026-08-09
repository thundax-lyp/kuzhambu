package com.thundax.kuzhambu.operations.application.cleanup.configure;

import com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupSupport;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.cleanup.schedule")
public class OperationsCleanupScheduleProperties {

    private boolean enabled = true;
    private boolean startupEnabled = false;
    private String dailyCron = "0 30 3 * * ?";
    private int defaultLimit = 200;
    private boolean expiredBackupEnabled = true;
    private int expiredBackupRetentionDays = 30;
    private Integer expiredBackupLimit = 200;
    private boolean expiredExportEnabled = true;
    private int expiredExportRetentionDays = 7;
    private Integer expiredExportLimit = 200;
    private boolean expiredShareEnabled = true;
    private int expiredShareRetentionDays = 90;
    private Integer expiredShareLimit = 200;
    private boolean expiredDraftEnabled = true;
    private int expiredDraftRetentionDays = 30;
    private Integer expiredDraftLimit = 200;
    private boolean expiredReportEnabled = true;
    private int expiredReportRetentionDays = 90;
    private Integer expiredReportLimit = 200;
    private boolean expiredHealthCheckEnabled = true;
    private int expiredHealthCheckRetentionDays = 30;
    private Integer expiredHealthCheckLimit = 500;
    private boolean expiredLongTaskEnabled = true;
    private int expiredLongTaskRetentionDays = 90;
    private Integer expiredLongTaskLimit = 200;

    public List<CleanupPolicy> orderedPolicies() {
        return OperationsCleanupSupport.orderedCleanupTypes().stream()
                .map(this::policyFor)
                .toList();
    }

    public CleanupPolicy policyFor(String cleanupType) {
        String normalizedType = OperationsCleanupSupport.normalizeType(cleanupType);
        return switch (normalizedType) {
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP ->
                new CleanupPolicy(
                        normalizedType,
                        expiredBackupEnabled,
                        expiredBackupRetentionDays,
                        effectiveLimit(expiredBackupLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT ->
                new CleanupPolicy(
                        normalizedType,
                        expiredExportEnabled,
                        expiredExportRetentionDays,
                        effectiveLimit(expiredExportLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_SHARE ->
                new CleanupPolicy(
                        normalizedType,
                        expiredShareEnabled,
                        expiredShareRetentionDays,
                        effectiveLimit(expiredShareLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT ->
                new CleanupPolicy(
                        normalizedType,
                        expiredDraftEnabled,
                        expiredDraftRetentionDays,
                        effectiveLimit(expiredDraftLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT ->
                new CleanupPolicy(
                        normalizedType,
                        expiredReportEnabled,
                        expiredReportRetentionDays,
                        effectiveLimit(expiredReportLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK ->
                new CleanupPolicy(
                        normalizedType,
                        expiredHealthCheckEnabled,
                        expiredHealthCheckRetentionDays,
                        effectiveLimit(expiredHealthCheckLimit));
            case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK ->
                new CleanupPolicy(
                        normalizedType,
                        expiredLongTaskEnabled,
                        expiredLongTaskRetentionDays,
                        effectiveLimit(expiredLongTaskLimit));
            default -> throw new IllegalArgumentException("Unsupported operations cleanup type: " + cleanupType);
        };
    }

    private int effectiveLimit(Integer policyLimit) {
        if (policyLimit == null || policyLimit <= 0) {
            return defaultLimit;
        }
        return policyLimit;
    }

    public record CleanupPolicy(String cleanupType, boolean enabled, int retentionDays, int limit) {}
}
