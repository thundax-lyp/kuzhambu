package com.thundax.kuzhambu.operations.application.backup.support;

import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifactResult;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;

public interface OperationsBackupScriptExecutor {

    OperationsBackupArtifactResult executeBackup(BackupType backupType, String timestamp);

    void executeRestore(String backupBaseName, String preRestoreTimestamp);

    OperationsBackupArtifactResult loadArtifact(String baseName);
}
