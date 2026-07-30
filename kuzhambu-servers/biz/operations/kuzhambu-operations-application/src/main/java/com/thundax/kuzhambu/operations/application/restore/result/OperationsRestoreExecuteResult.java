package com.thundax.kuzhambu.operations.application.restore.result;

import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestoreExecuteResult {
    private RestoreId restoreId;
    private Long backupId;
    private Long preRestoreBackupId;
    private String restoreMode;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private Instant writeBlockStartedAt;
    private Instant writeBlockReleasedAt;
    private String failureReason;
    private Instant startedAt;
    private Instant completedAt;

    public OperationsRestoreExecuteResult(
            RestoreId restoreId,
            Long backupId,
            Long preRestoreBackupId,
            String restoreStatus,
            Boolean writeBlockEnabled,
            String failureReason,
            Instant startedAt,
            Instant completedAt) {
        this.restoreId = restoreId;
        this.backupId = backupId;
        this.preRestoreBackupId = preRestoreBackupId;
        this.restoreStatus = restoreStatus;
        this.writeBlockEnabled = writeBlockEnabled;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}
