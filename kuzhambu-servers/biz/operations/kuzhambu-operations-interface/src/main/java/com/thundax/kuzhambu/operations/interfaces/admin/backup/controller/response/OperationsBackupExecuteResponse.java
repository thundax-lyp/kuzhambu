package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsBackupExecuteResponse", description = "Operations 备份执行响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsBackupExecuteResponse {
    @JsonSerialize(using = ToStringSerializer.class)
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
