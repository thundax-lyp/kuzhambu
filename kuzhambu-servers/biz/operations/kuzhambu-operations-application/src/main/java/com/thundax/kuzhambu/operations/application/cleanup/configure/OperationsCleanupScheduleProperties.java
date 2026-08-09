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
    private Policies policies = new Policies();

    public List<CleanupPolicy> orderedPolicies() {
        return OperationsCleanupSupport.orderedCleanupTypes().stream()
                .map(this::policyFor)
                .toList();
    }

    public CleanupPolicy policyFor(String cleanupType) {
        String normalizedType = OperationsCleanupSupport.normalizeType(cleanupType);
        CleanupPolicyProperties policy =
                switch (normalizedType) {
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP -> policies.getExpiredBackup();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT -> policies.getExpiredExport();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_SHARE -> policies.getExpiredShare();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT -> policies.getExpiredDraft();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT -> policies.getExpiredReport();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK -> policies.getExpiredHealthCheck();
                    case OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK -> policies.getExpiredLongTask();
                    default ->
                        throw new IllegalArgumentException("Unsupported operations cleanup type: " + cleanupType);
                };
        return new CleanupPolicy(
                normalizedType, policy.isEnabled(), policy.getRetentionDays(), effectiveLimit(policy.getLimit()));
    }

    private int effectiveLimit(Integer policyLimit) {
        if (policyLimit == null || policyLimit <= 0) {
            return defaultLimit;
        }
        return policyLimit;
    }

    public record CleanupPolicy(String cleanupType, boolean enabled, int retentionDays, int limit) {}

    @Getter
    @Setter
    public static class Policies {

        private CleanupPolicyProperties expiredBackup = new CleanupPolicyProperties(true, 30, 200);
        private CleanupPolicyProperties expiredExport = new CleanupPolicyProperties(true, 7, 200);
        private CleanupPolicyProperties expiredShare = new CleanupPolicyProperties(true, 90, 200);
        private CleanupPolicyProperties expiredDraft = new CleanupPolicyProperties(true, 30, 200);
        private CleanupPolicyProperties expiredReport = new CleanupPolicyProperties(true, 90, 200);
        private CleanupPolicyProperties expiredHealthCheck = new CleanupPolicyProperties(true, 30, 500);
        private CleanupPolicyProperties expiredLongTask = new CleanupPolicyProperties(true, 90, 200);
    }

    @Getter
    @Setter
    public static class CleanupPolicyProperties {

        private boolean enabled;
        private int retentionDays;
        private Integer limit;

        public CleanupPolicyProperties() {}

        private CleanupPolicyProperties(boolean enabled, int retentionDays, Integer limit) {
            this.enabled = enabled;
            this.retentionDays = retentionDays;
            this.limit = limit;
        }
    }
}
