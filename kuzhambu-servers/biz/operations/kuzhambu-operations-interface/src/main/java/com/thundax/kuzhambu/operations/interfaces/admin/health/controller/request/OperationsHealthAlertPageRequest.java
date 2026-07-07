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
@Schema(name = "OperationsHealthAlertPageRequest", description = "Operations 健康告警分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthAlertPageRequest extends PageRequest {

    @Schema(name = "component", description = "组件筛选")
    @JsonProperty(value = "component")
    private String component;

    @Schema(name = "alertLevel", description = "告警级别筛选")
    @JsonProperty(value = "alertLevel")
    private String alertLevel;

    @Schema(name = "alertStatus", description = "告警状态筛选")
    @JsonProperty(value = "alertStatus")
    private String alertStatus;

    @Schema(name = "sourceRefType", description = "来源类型筛选")
    @JsonProperty(value = "sourceRefType")
    private String sourceRefType;

    @Schema(name = "sourceRefId", description = "来源业务 ID 筛选")
    @JsonProperty(value = "sourceRefId")
    private Long sourceRefId;

    @Schema(name = "latestCheckId", description = "最近关联健康检查 ID 筛选")
    @JsonProperty(value = "latestCheckId")
    private Long latestCheckId;
}
