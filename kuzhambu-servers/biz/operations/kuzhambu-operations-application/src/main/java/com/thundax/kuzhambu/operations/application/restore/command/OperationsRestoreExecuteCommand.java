package com.thundax.kuzhambu.operations.application.restore.command;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestoreExecuteCommand {
    private BackupId backupId;
    private Long requesterUserId;
}
