package com.thundax.kuzhambu.operations.application.restore.command;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;

public record OperationsRestoreExecuteCommand(BackupId backupId, String restoreMode, Long requesterUserId) {

    public OperationsRestoreExecuteCommand(BackupId backupId, Long requesterUserId) {
        this(backupId, null, requesterUserId);
    }
}
