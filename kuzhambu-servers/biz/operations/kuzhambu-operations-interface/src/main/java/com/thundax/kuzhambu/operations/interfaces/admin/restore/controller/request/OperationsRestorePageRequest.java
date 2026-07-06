package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsRestorePageRequest", description = "Operations 恢复分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsRestorePageRequest extends PageRequest {

    @Schema(name = "backupId", description = "来源备份筛选")
    @JsonProperty(value = "backupId")
    private Long backupId;

    @Schema(name = "restoreMode", description = "恢复模式筛选")
    @JsonProperty(value = "restoreMode")
    private String restoreMode;

    @Schema(name = "restoreStatus", description = "恢复状态筛选")
    @JsonProperty(value = "restoreStatus")
    private String restoreStatus;

    @Schema(name = "requesterUserId", description = "请求人筛选")
    @JsonProperty(value = "requesterUserId")
    private Long requesterUserId;
}
