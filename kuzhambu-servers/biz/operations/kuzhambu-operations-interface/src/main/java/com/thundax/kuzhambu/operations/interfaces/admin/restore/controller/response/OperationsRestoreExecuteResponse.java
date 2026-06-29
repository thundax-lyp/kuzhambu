package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestoreExecuteResponse {
    private Long restoreId;
    private Long backupId;
    private Long preRestoreBackupId;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private String failureReason;
    private Date startedAt;
    private Date completedAt;
}
