package com.thundax.kuzhambu.operations.application.backup.support;

import com.thundax.kuzhambu.operations.application.backup.configure.OperationsBackupScheduleProperties;
import com.thundax.kuzhambu.operations.application.backup.service.BackupSchedulerApplicationService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OperationsBackupScheduler implements ApplicationListener<ApplicationReadyEvent> {

    private final BackupSchedulerApplicationService backupSchedulerApplicationService;
    private final OperationsBackupScheduleProperties properties;

    public OperationsBackupScheduler(
            BackupSchedulerApplicationService backupSchedulerApplicationService,
            OperationsBackupScheduleProperties properties) {
        this.backupSchedulerApplicationService = backupSchedulerApplicationService;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isEnabled() || !properties.isStartupEnabled()) {
            return;
        }
        backupSchedulerApplicationService.runScheduledBackup();
    }

    @Scheduled(cron = "${kuzhambu.operations.backup.schedule.daily-cron:0 0 2 * * ?}")
    public void executeDailyBackup() {
        if (!properties.isEnabled()) {
            return;
        }
        backupSchedulerApplicationService.runScheduledBackup();
    }
}
