package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response;

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
public class OperationsBackupDetailResponse {
    private Long backupId;
    private String backupType;
    private String backupStatus;
    private Long storageObjectId;
    private String fileName;
    private Long fileSizeBytes;
    private String checksum;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
    private Date expiresAt;
}
