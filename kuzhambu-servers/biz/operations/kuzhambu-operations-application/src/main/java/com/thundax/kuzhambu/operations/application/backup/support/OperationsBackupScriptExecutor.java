package com.thundax.kuzhambu.operations.application.backup.support;

import com.thundax.kuzhambu.operations.application.backup.support.OperationsBackupSupportModels.OperationsBackupArtifact;
import com.thundax.kuzhambu.operations.domain.backup.model.enums.BackupType;

public interface OperationsBackupScriptExecutor {

    OperationsBackupArtifact executeBackup(BackupType backupType, String timestamp);

    void executeRestore(String backupBaseName, String preRestoreTimestamp);

    void executeRestoreDrill(String backupBaseName, String preRestoreTimestamp);

    OperationsBackupArtifact loadArtifact(String baseName);
}
