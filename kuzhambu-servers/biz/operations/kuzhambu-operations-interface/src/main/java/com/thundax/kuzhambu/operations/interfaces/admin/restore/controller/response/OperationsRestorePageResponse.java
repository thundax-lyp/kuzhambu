package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.response;

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
@Schema(name = "OperationsRestorePageResponse", description = "Operations 恢复分页响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsRestorePageResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long restoreId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long backupId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long preRestoreBackupId;

    private String restoreMode;
    private String restoreStatus;
    private Boolean writeBlockEnabled;
    private Instant writeBlockStartedAt;
    private Instant writeBlockReleasedAt;
    private String failureReason;
    private Long requesterUserId;
    private Instant startedAt;
    private Instant completedAt;
}
