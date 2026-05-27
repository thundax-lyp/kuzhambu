package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuditLogPageRequest", description = "审计日志分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogPageRequest extends PageRequest {

    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @Schema(name = "objectId", description = "对象ID")
    private String objectId;

    @Schema(name = "action", description = "审计动作")
    private String action;

    @Schema(name = "operatorType", description = "操作者类型")
    private String operatorType;

    @Schema(name = "operatorId", description = "操作者ID")
    private String operatorId;

    @Schema(name = "source", description = "来源")
    private String source;

    @Schema(name = "requestId", description = "请求ID")
    private String requestId;

    @Schema(name = "beginDate", description = "开始时间")
    private Date beginDate;

    @Schema(name = "endDate", description = "结束时间")
    private Date endDate;
}
