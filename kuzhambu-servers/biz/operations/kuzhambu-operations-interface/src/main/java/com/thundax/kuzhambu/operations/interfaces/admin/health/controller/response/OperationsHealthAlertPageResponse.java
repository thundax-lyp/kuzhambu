package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperationsHealthAlertPageResponse", description = "Operations 健康告警分页响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthAlertPageResponse {

    @Schema(name = "alertId", description = "告警业务 ID")
    @JsonProperty(value = "alertId")
    private Long alertId;

    @Schema(name = "component", description = "异常组件")
    @JsonProperty(value = "component")
    private String component;

    @Schema(name = "alertType", description = "告警类型")
    @JsonProperty(value = "alertType")
    private String alertType;

    @Schema(name = "alertLevel", description = "告警级别：WARNING/CRITICAL")
    @JsonProperty(value = "alertLevel")
    private String alertLevel;

    @Schema(name = "alertStatus", description = "告警状态：ACTIVE/ACKED/RECOVERED")
    @JsonProperty(value = "alertStatus")
    private String alertStatus;

    @Schema(name = "sourceRefType", description = "来源类型")
    @JsonProperty(value = "sourceRefType")
    private String sourceRefType;

    @Schema(name = "sourceRefId", description = "来源业务 ID")
    @JsonProperty(value = "sourceRefId")
    private Long sourceRefId;

    @Schema(name = "latestCheckId", description = "最近关联健康检查 ID")
    @JsonProperty(value = "latestCheckId")
    private Long latestCheckId;

    @Schema(name = "message", description = "告警消息")
    @JsonProperty(value = "message")
    private String message;

    @Schema(name = "suggestion", description = "处置建议")
    @JsonProperty(value = "suggestion")
    private String suggestion;

    @Schema(name = "recoveryAction", description = "恢复动作")
    @JsonProperty(value = "recoveryAction")
    private String recoveryAction;

    @Schema(name = "recoveryTarget", description = "恢复目标 JSON")
    @JsonProperty(value = "recoveryTarget")
    private String recoveryTarget;

    @Schema(name = "firstTriggeredAt", description = "首次触发时间")
    @JsonProperty(value = "firstTriggeredAt")
    private Date firstTriggeredAt;

    @Schema(name = "lastTriggeredAt", description = "最近触发时间")
    @JsonProperty(value = "lastTriggeredAt")
    private Date lastTriggeredAt;

    @Schema(name = "ackedAt", description = "确认时间")
    @JsonProperty(value = "ackedAt")
    private Date ackedAt;

    @Schema(name = "ackedByUserId", description = "确认管理员 ID")
    @JsonProperty(value = "ackedByUserId")
    private Long ackedByUserId;

    @Schema(name = "recoveredAt", description = "恢复时间")
    @JsonProperty(value = "recoveredAt")
    private Date recoveredAt;

    @Schema(name = "failureReason", description = "失败原因")
    @JsonProperty(value = "failureReason")
    private String failureReason;
}
