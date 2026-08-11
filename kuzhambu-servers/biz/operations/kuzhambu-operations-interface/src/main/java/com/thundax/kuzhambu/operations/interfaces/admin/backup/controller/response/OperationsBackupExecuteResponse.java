package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
@Schema(name = "OperationsBackupExecuteResponse", description = "Operations 备份执行响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsBackupExecuteResponse {
    private Long backupId;
    private String backupType;
    private String backupStatus;
    private String fileName;
    private Long fileSizeBytes;
    private String checksum;
    private String failureReason;
    private Instant startedAt;
    private Instant completedAt;
    private Instant expiresAt;
}
