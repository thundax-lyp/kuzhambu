package com.thundax.kuzhambu.operations.application.cleanup.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupConfiguration;
import com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupScheduleProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OperationsCleanupSchedulePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(OperationsCleanupConfiguration.class);

    @Test
    void shouldBindNestedPolicyProperties() {
        contextRunner
                .withPropertyValues(
                        "kuzhambu.operations.cleanup.schedule.policies.expired-backup.enabled=false",
                        "kuzhambu.operations.cleanup.schedule.policies.expired-backup.retention-days=60",
                        "kuzhambu.operations.cleanup.schedule.policies.expired-backup.limit=500")
                .run(context -> {
                    OperationsCleanupScheduleProperties properties =
                            context.getBean(OperationsCleanupScheduleProperties.class);

                    OperationsCleanupScheduleProperties.CleanupPolicy policy =
                            properties.policyFor(OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP);

                    assertFalse(policy.enabled());
                    assertEquals(60, policy.retentionDays());
                    assertEquals(500, policy.limit());
                });
    }

    @Test
    void orderedPoliciesShouldFollowCleanupExecutionOrderAndFallbackLimit() {
        OperationsCleanupScheduleProperties properties = new OperationsCleanupScheduleProperties();
        properties.setDefaultLimit(200);
        properties.getPolicies().getExpiredBackup().setLimit(100);
        properties.getPolicies().getExpiredExport().setLimit(null);
        properties.getPolicies().getExpiredShare().setLimit(0);
        properties.getPolicies().getExpiredDraft().setEnabled(false);
        properties.getPolicies().getExpiredDraft().setLimit(50);
        properties.getPolicies().getExpiredReport().setLimit(null);
        properties.getPolicies().getExpiredHealthCheck().setLimit(500);
        properties.getPolicies().getExpiredLongTask().setLimit(0);

        List<OperationsCleanupScheduleProperties.CleanupPolicy> policies = properties.orderedPolicies();

        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP,
                policies.get(0).cleanupType());
        assertEquals(100, policies.get(0).limit());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT,
                policies.get(1).cleanupType());
        assertEquals(200, policies.get(1).limit());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_SHARE,
                policies.get(2).cleanupType());
        assertEquals(200, policies.get(2).limit());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT,
                policies.get(3).cleanupType());
        assertFalse(policies.get(3).enabled());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT,
                policies.get(4).cleanupType());
        assertEquals(200, policies.get(4).limit());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK,
                policies.get(5).cleanupType());
        assertEquals(500, policies.get(5).limit());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK,
                policies.get(6).cleanupType());
        assertEquals(200, policies.get(6).limit());
    }
}
