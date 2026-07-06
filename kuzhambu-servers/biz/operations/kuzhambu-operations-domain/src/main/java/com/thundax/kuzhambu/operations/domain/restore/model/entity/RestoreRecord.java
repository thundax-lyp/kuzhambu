package com.thundax.kuzhambu.operations.domain.restore.model.entity;

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
public class RestoreRecord {

    private RestoreId id;
    private Long backupId;
    private Long preRestoreBackupId;
    private String restoreMode;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private Date writeBlockStartedAt;
    private Date writeBlockReleasedAt;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
}
