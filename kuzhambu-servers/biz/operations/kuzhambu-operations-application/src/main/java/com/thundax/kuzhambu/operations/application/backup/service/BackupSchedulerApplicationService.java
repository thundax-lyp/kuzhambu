package com.thundax.kuzhambu.operations.application.backup.service;

import com.thundax.kuzhambu.operations.application.backup.result.OperationsBackupExecuteResult;

public interface BackupSchedulerApplicationService {

    OperationsBackupExecuteResult runScheduledBackup();
}
