package com.thundax.kuzhambu.operations.application.restore.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestoreQuery {
    private Long backupId;
    private String restoreMode;
    private String restoreStatus;
    private Long requesterUserId;

    public OperationsRestoreQuery(Long backupId, String restoreStatus, Long requesterUserId) {
        this.backupId = backupId;
        this.restoreStatus = restoreStatus;
        this.requesterUserId = requesterUserId;
    }
}
