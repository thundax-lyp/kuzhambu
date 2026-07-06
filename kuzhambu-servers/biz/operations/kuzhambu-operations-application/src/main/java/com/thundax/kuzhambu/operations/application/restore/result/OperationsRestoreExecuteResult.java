package com.thundax.kuzhambu.operations.application.restore.result;

import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import java.util.Date;
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
    private Date writeBlockStartedAt;
    private Date writeBlockReleasedAt;
    private String failureReason;
    private Date startedAt;
    private Date completedAt;

    public OperationsRestoreExecuteResult(
            RestoreId restoreId,
            Long backupId,
            Long preRestoreBackupId,
            String restoreStatus,
            Boolean writeBlockEnabled,
            String failureReason,
            Date startedAt,
            Date completedAt) {
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
