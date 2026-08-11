package com.thundax.kuzhambu.operations.application.backup.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.backup.command.OperationsBackupExecuteCommand;
import com.thundax.kuzhambu.operations.application.backup.configure.OperationsBackupScheduleProperties;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupDetailQuery;
import com.thundax.kuzhambu.operations.application.backup.query.OperationsBackupQuery;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupDetailResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;
import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupPageResult;
import com.thundax.kuzhambu.operations.application.backup.service.BackupApplicationService;
import com.thundax.kuzhambu.operations.application.backup.service.BackupSchedulerApplicationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class OperationsBackupSchedulerTest {

    @Test
    void onApplicationReadyShouldExecuteAutoBackup() {
        CountingBackupApplicationService backupSchedulerApplicationService = new CountingBackupApplicationService();
        OperationsBackupScheduler scheduler =
                new OperationsBackupScheduler(backupSchedulerApplicationService, properties(true, true));

        scheduler.onApplicationEvent(null);

        assertEquals(1, backupSchedulerApplicationService.autoBackupCount);
    }

    @Test
    void onApplicationReadyShouldSkipWhenScheduleDisabled() {
        CountingBackupApplicationService backupSchedulerApplicationService = new CountingBackupApplicationService();
        OperationsBackupScheduler scheduler =
                new OperationsBackupScheduler(backupSchedulerApplicationService, properties(false, true));

        scheduler.onApplicationEvent(null);

        assertEquals(0, backupSchedulerApplicationService.autoBackupCount);
    }

    @Test
    void onApplicationReadyShouldSkipWhenStartupDisabled() {
        CountingBackupApplicationService backupSchedulerApplicationService = new CountingBackupApplicationService();
        OperationsBackupScheduler scheduler =
                new OperationsBackupScheduler(backupSchedulerApplicationService, properties(true, false));

        scheduler.onApplicationEvent(null);

        assertEquals(0, backupSchedulerApplicationService.autoBackupCount);
    }

    @Test
    void executeDailyBackupShouldExecuteAutoBackup() {
        CountingBackupApplicationService backupSchedulerApplicationService = new CountingBackupApplicationService();
        OperationsBackupScheduler scheduler =
                new OperationsBackupScheduler(backupSchedulerApplicationService, properties(true, true));

        scheduler.executeDailyBackup();

        assertEquals(1, backupSchedulerApplicationService.autoBackupCount);
    }

    private OperationsBackupScheduleProperties properties(boolean enabled, boolean startupEnabled) {
        OperationsBackupScheduleProperties properties = new OperationsBackupScheduleProperties();
        properties.setEnabled(enabled);
        properties.setStartupEnabled(startupEnabled);
        properties.setDailyCron("0 0 2 * * ?");
        return properties;
    }

    private static final class CountingBackupApplicationService
            implements BackupApplicationService, BackupSchedulerApplicationService {
        private int autoBackupCount;

        @Override
        public OperationsBackupExecuteResult execute(OperationsBackupExecuteCommand command) {
            return null;
        }

        @Override
        public OperationsBackupExecuteResult runScheduledBackup() {
            autoBackupCount++;
            return null;
        }

        @Override
        public PageResult<OperationsBackupPageResult> page(OperationsBackupQuery query, PageQuery pageQuery) {
            return PageResult.of(1, 10, 0, List.of());
        }

        @Override
        public OperationsBackupDetailResult detail(OperationsBackupDetailQuery query) {
            return null;
        }
    }
}
