package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsHealthAlertRecoverRequest", description = "Operations 健康告警人工恢复请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthAlertRecoverRequest {

    @NotNull(message = "告警 ID 不能为空")
    @Schema(name = "alertId", description = "告警业务 ID")
    @JsonProperty(value = "alertId")
    private Long alertId;
}
