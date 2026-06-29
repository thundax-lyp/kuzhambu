package com.thundax.kuzhambu.operations.application.backup.result;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsBackupExecuteResult {
    private BackupId backupId;
    private String backupType;
    private String backupStatus;
    private String fileName;
    private Long fileSizeBytes;
    private String checksum;
    private String failureReason;
    private Date startedAt;
    private Date completedAt;
    private Date expiresAt;
}
