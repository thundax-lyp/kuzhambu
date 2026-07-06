package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsRestoreExecuteRequest", description = "Operations 恢复执行请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsRestoreExecuteRequest {

    @NotNull
    @Schema(name = "backupId", description = "恢复来源备份 ID")
    @JsonProperty(value = "backupId")
    private Long backupId;

    @Schema(name = "restoreMode", description = "恢复模式：REAL 或 DRILL")
    @JsonProperty(value = "restoreMode")
    private String restoreMode;
}
