package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditLogResponse", description = "审计日志响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogResponse implements Serializable {

    @Schema(name = "id", description = "ID")
    private String id;

    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @Schema(name = "objectId", description = "对象ID")
    private String objectId;

    @Schema(name = "objectDisplayName", description = "对象展示名")
    private String objectDisplayName;

    @Schema(name = "objectTypeLabel", description = "对象类型标签")
    private String objectTypeLabel;

    @Schema(name = "version", description = "审计版本")
    private Long version;

    @Schema(name = "action", description = "动作")
    private String action;

    @Schema(name = "actionLabel", description = "动作标签")
    private String actionLabel;

    @Schema(name = "operatorType", description = "操作者类型")
    private String operatorType;

    @Schema(name = "operatorTypeLabel", description = "操作者类型标签")
    private String operatorTypeLabel;

    @Schema(name = "operatorId", description = "操作者ID")
    private String operatorId;

    @Schema(name = "operatorName", description = "操作者")
    private String operatorName;

    @Schema(name = "source", description = "来源")
    private String source;

    @Schema(name = "requestId", description = "请求ID")
    private String requestId;

    @Schema(name = "traceId", description = "链路ID")
    private String traceId;

    @Schema(name = "remoteAddr", description = "远端地址")
    private String remoteAddr;

    @Schema(name = "summary", description = "摘要")
    private String summary;

    @Schema(name = "occurredAt", description = "发生时间")
    private Date occurredAt;

    @Schema(name = "changedFields", description = "变更字段")
    private List<AuditFieldResponse> changedFields = new ArrayList<>();
}
