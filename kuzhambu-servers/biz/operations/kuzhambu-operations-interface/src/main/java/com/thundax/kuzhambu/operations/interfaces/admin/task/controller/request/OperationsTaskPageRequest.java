package com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsTaskPageRequest", description = "Operations 长任务分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsTaskPageRequest extends PageRequest {

    @Schema(name = "sourceDomain", description = "来源域筛选")
    @JsonProperty(value = "sourceDomain")
    private String sourceDomain;

    @Schema(name = "taskType", description = "任务类型筛选")
    @JsonProperty(value = "taskType")
    private String taskType;

    @Schema(name = "taskStatus", description = "任务状态筛选")
    @JsonProperty(value = "taskStatus")
    private String taskStatus;
}
