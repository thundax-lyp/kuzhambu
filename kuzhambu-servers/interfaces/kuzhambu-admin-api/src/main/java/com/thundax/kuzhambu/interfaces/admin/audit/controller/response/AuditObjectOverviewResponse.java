package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditObjectOverviewResponse", description = "对象审计概览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectOverviewResponse implements Serializable {

    @Schema(name = "meta", description = "审计元数据")
    private AuditMetaResponse meta;

    @Schema(name = "latestLogs", description = "最近审计日志")
    private List<AuditLogResponse> latestLogs = new ArrayList<>();
}
