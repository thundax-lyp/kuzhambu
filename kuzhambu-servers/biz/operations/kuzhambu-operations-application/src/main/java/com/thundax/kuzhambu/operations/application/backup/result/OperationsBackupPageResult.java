package com.thundax.kuzhambu.operations.application.backup.result;

import com.thundax.kuzhambu.operations.domain.backup.model.valueobject.BackupId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsBackupPageResult {
    private BackupId backupId;
    private String backupType;
    private String backupStatus;
    private String fileName;
    private Long fileSizeBytes;
    private String checksum;
    private String failureReason;
    private Long requesterUserId;
    private Instant startedAt;
    private Instant completedAt;
    private Instant expiresAt;
}
