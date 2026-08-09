package com.thundax.kuzhambu.operations.application.backup.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsBackupQuery {
    private String backupType;
    private String backupStatus;
    private Long requesterUserId;
}
