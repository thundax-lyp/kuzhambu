package com.thundax.kuzhambu.operations.application.cleanup.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsCleanupSchedulePropertiesTest {

    @Test
    void orderedPoliciesShouldFollowCleanupExecutionOrderAndFallbackLimit() {
        OperationsCleanupScheduleProperties properties = new OperationsCleanupScheduleProperties();
        properties.setDefaultLimit(200);
        properties.setExpiredBackupEnabled(true);
        properties.setExpiredBackupRetentionDays(30);
        properties.setExpiredBackupLimit(100);
        properties.setExpiredExportEnabled(true);
        properties.setExpiredExportRetentionDays(7);
        properties.setExpiredExportLimit(null);
        properties.setExpiredShareEnabled(true);
        properties.setExpiredShareRetentionDays(90);
        properties.setExpiredShareLimit(0);
        properties.setExpiredDraftEnabled(false);
        properties.setExpiredDraftRetentionDays(30);
        properties.setExpiredDraftLimit(50);

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
    }
}
