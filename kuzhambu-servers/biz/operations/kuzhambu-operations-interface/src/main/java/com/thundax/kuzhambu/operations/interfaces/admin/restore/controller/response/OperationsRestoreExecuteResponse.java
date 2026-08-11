package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsRestoreExecuteResponse", description = "Operations 恢复执行响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsRestoreExecuteResponse {
    private Long restoreId;
    private Long backupId;
    private Long preRestoreBackupId;
    private String restoreMode;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private Instant writeBlockStartedAt;
    private Instant writeBlockReleasedAt;
    private String failureReason;
    private Instant startedAt;
    private Instant completedAt;
}
