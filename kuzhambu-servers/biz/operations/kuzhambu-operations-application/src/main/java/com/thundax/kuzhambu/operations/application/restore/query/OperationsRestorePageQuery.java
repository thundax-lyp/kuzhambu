package com.thundax.kuzhambu.operations.application.restore.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestorePageQuery {
    private Long backupId;
    private String restoreStatus;
    private Long requesterUserId;
}
