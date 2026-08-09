package com.thundax.kuzhambu.operations.application.cleanup.configure;

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
