package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsCleanupPageRequest", description = "Operations 清理分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsCleanupPageRequest extends PageRequest {

    @Schema(name = "cleanupType", description = "清理类型筛选")
    @JsonProperty(value = "cleanupType")
    private String cleanupType;

    @Schema(name = "cleanupStatus", description = "清理状态筛选")
    @JsonProperty(value = "cleanupStatus")
    private String cleanupStatus;

    @Schema(name = "requesterUserId", description = "请求人筛选")
    @JsonProperty(value = "requesterUserId")
    private Long requesterUserId;
}
