package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsBackupDetailRequest", description = "Operations 备份详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsBackupDetailRequest {

    @NotNull
    @Schema(name = "backupId", description = "备份记录 ID")
    @JsonProperty(value = "backupId")
    private Long backupId;
}
