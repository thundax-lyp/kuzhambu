package com.thundax.kuzhambu.operations.application.restore.query;

public record OperationsRestoreQuery(Long backupId, String restoreMode, String restoreStatus, Long requesterUserId) {

    public OperationsRestoreQuery(Long backupId, String restoreStatus, Long requesterUserId) {
        this(backupId, null, restoreStatus, requesterUserId);
    }
}
