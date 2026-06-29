package com.thundax.kuzhambu.operations.interfaces.admin.backup.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsBackupPageRequest", description = "Operations 备份分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsBackupPageRequest extends PageRequest {

    @Schema(name = "backupType", description = "备份类型筛选")
    @JsonProperty(value = "backupType")
    private String backupType;

    @Schema(name = "backupStatus", description = "备份状态筛选")
    @JsonProperty(value = "backupStatus")
    private String backupStatus;

    @Schema(name = "requesterUserId", description = "请求人筛选")
    @JsonProperty(value = "requesterUserId")
    private Long requesterUserId;
}
