package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsBackupDetailResponse", description = "Operations 备份明细响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private Instant startedAt;
    private Instant completedAt;
    private Instant expiresAt;
}
