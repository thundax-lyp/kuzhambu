package com.thundax.kuzhambu.operations.application.backup.query;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsBackupDetailQuery {
    private BackupId backupId;
}
