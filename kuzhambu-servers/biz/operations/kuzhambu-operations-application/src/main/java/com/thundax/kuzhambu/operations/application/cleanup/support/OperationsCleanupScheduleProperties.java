package com.thundax.kuzhambu.operations.application.cleanup.support;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class OperationsCleanupScheduleProperties {

    @Value("${kuzhambu.operations.cleanup.schedule.enabled:true}")
    private boolean enabled;

    @Value("${kuzhambu.operations.cleanup.schedule.startup-enabled:false}")
    private boolean startupEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.daily-cron:0 30 3 * * ?}")
    private String dailyCron;

    @Value("${kuzhambu.operations.cleanup.schedule.default-limit:200}")
    private int defaultLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-backup.enabled:true}")
    private boolean expiredBackupEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-backup.retention-days:30}")
    private int expiredBackupRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-backup.limit:200}")
    private Integer expiredBackupLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-export.enabled:true}")
    private boolean expiredExportEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-export.retention-days:7}")
    private int expiredExportRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-export.limit:200}")
    private Integer expiredExportLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-share.enabled:true}")
    private boolean expiredShareEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-share.retention-days:90}")
    private int expiredShareRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-share.limit:200}")
    private Integer expiredShareLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-draft.enabled:true}")
    private boolean expiredDraftEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-draft.retention-days:30}")
    private int expiredDraftRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-draft.limit:200}")
    private Integer expiredDraftLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-report.enabled:true}")
    private boolean expiredReportEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-report.retention-days:90}")
    private int expiredReportRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-report.limit:200}")
    private Integer expiredReportLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-health-check.enabled:true}")
    private boolean expiredHealthCheckEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-health-check.retention-days:30}")
    private int expiredHealthCheckRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-health-check.limit:500}")
    private Integer expiredHealthCheckLimit;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-long-task.enabled:true}")
    private boolean expiredLongTaskEnabled;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-long-task.retention-days:90}")
    private int expiredLongTaskRetentionDays;

    @Value("${kuzhambu.operations.cleanup.schedule.policies.expired-long-task.limit:200}")
    private Integer expiredLongTaskLimit;

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
