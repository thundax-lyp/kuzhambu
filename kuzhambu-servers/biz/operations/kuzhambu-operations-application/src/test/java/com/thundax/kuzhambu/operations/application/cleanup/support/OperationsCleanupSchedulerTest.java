package com.thundax.kuzhambu.operations.application.cleanup.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupDetailQuery;
import com.thundax.kuzhambu.operations.application.cleanup.query.OperationsCleanupQuery;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupDetailResult;
import com.thundax.kuzhambu.operations.application.cleanup.result.OperationsCleanupPageResult;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupScheduleProperties;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsCleanupSchedulerTest {

    @Test
    void onApplicationReadyShouldSkipWhenStartupDisabled() {
        CountingCleanupApplicationService cleanupApplicationService = new CountingCleanupApplicationService();
        OperationsCleanupScheduler scheduler =
                new OperationsCleanupScheduler(cleanupApplicationService, properties(true, false));

        scheduler.onApplicationEvent(null);

        assertEquals(0, cleanupApplicationService.commands.size());
    }

    @Test
    void executeDailyCleanupShouldSkipWhenScheduleDisabled() {
        CountingCleanupApplicationService cleanupApplicationService = new CountingCleanupApplicationService();
        OperationsCleanupScheduler scheduler =
                new OperationsCleanupScheduler(cleanupApplicationService, properties(false, true));

        scheduler.executeDailyCleanup();

        assertEquals(0, cleanupApplicationService.commands.size());
    }

    @Test
    void executeDailyCleanupShouldExecuteEnabledPoliciesInOrder() {
        CountingCleanupApplicationService cleanupApplicationService = new CountingCleanupApplicationService();
        OperationsCleanupScheduleProperties properties = properties(true, true);
        properties.setExpiredShareEnabled(false);
        OperationsCleanupScheduler scheduler = new OperationsCleanupScheduler(cleanupApplicationService, properties);

        scheduler.executeDailyCleanup();

        assertEquals(6, cleanupApplicationService.commands.size());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_BACKUP,
                cleanupApplicationService.commands.get(0).getCleanupType());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT,
                cleanupApplicationService.commands.get(1).getCleanupType());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_DRAFT,
                cleanupApplicationService.commands.get(2).getCleanupType());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_REPORT,
                cleanupApplicationService.commands.get(3).getCleanupType());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_HEALTH_CHECK,
                cleanupApplicationService.commands.get(4).getCleanupType());
        assertEquals(
                OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_LONG_TASK,
                cleanupApplicationService.commands.get(5).getCleanupType());
    }

    @Test
    void executeDailyCleanupShouldContinueAfterSinglePolicyFailure() {
        CountingCleanupApplicationService cleanupApplicationService = new CountingCleanupApplicationService();
        cleanupApplicationService.failedCleanupType = OperationsCleanupSupport.CLEANUP_TYPE_EXPIRED_EXPORT;
        OperationsCleanupScheduler scheduler =
                new OperationsCleanupScheduler(cleanupApplicationService, properties(true, true));

        scheduler.executeDailyCleanup();

        assertEquals(7, cleanupApplicationService.commands.size());
    }

    private OperationsCleanupScheduleProperties properties(boolean enabled, boolean startupEnabled) {
        OperationsCleanupScheduleProperties properties = new OperationsCleanupScheduleProperties();
        properties.setEnabled(enabled);
        properties.setStartupEnabled(startupEnabled);
        properties.setDailyCron("0 30 3 * * ?");
        properties.setDefaultLimit(200);
        properties.setExpiredBackupEnabled(true);
        properties.setExpiredBackupRetentionDays(30);
        properties.setExpiredBackupLimit(200);
        properties.setExpiredExportEnabled(true);
        properties.setExpiredExportRetentionDays(7);
        properties.setExpiredExportLimit(200);
        properties.setExpiredShareEnabled(true);
        properties.setExpiredShareRetentionDays(90);
        properties.setExpiredShareLimit(200);
        properties.setExpiredDraftEnabled(true);
        properties.setExpiredDraftRetentionDays(30);
        properties.setExpiredDraftLimit(200);
        properties.setExpiredReportEnabled(true);
        properties.setExpiredReportRetentionDays(90);
        properties.setExpiredReportLimit(200);
        properties.setExpiredHealthCheckEnabled(true);
        properties.setExpiredHealthCheckRetentionDays(30);
        properties.setExpiredHealthCheckLimit(500);
        properties.setExpiredLongTaskEnabled(true);
        properties.setExpiredLongTaskRetentionDays(90);
        properties.setExpiredLongTaskLimit(200);
        return properties;
    }

    private static final class CountingCleanupApplicationService implements CleanupApplicationService {
        private final List<OperationsCleanupExecuteCommand> commands = new ArrayList<>();
        private String failedCleanupType;

        @Override
        public OperationsCleanupDetailResult execute(OperationsCleanupExecuteCommand command) {
            return null;
        }

        @Override
        public OperationsCleanupDetailResult executeScheduled(OperationsCleanupExecuteCommand command) {
            commands.add(command);
            if (command.getCleanupType().equals(failedCleanupType)) {
                throw new IllegalStateException("cleanup failed");
            }
            return null;
        }

        @Override
        public PageResult<OperationsCleanupPageResult> page(OperationsCleanupQuery query, PageQuery pageQuery) {
            return PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public OperationsCleanupDetailResult detail(OperationsCleanupDetailQuery query) {
            return null;
        }
    }
}
