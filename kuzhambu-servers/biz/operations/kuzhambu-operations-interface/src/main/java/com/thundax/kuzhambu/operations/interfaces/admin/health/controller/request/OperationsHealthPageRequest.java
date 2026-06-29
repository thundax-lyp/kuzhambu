package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsHealthPageRequest", description = "Operations 健康记录分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthPageRequest extends PageRequest {

    @Schema(name = "component", description = "组件筛选")
    @JsonProperty(value = "component")
    private String component;

    @Schema(name = "healthStatus", description = "健康状态筛选")
    @JsonProperty(value = "healthStatus")
    private String healthStatus;
}
