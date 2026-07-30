package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsReportPageRequest", description = "Operations 报表分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsReportPageRequest extends PageRequest {

    @Schema(name = "reportType", description = "报表类型筛选")
    @JsonProperty(value = "reportType")
    private String reportType;

    @Schema(name = "format", description = "导出格式筛选")
    @JsonProperty(value = "format")
    private String format;

    @Schema(name = "reportStatus", description = "报表状态筛选")
    @JsonProperty(value = "reportStatus")
    private String reportStatus;

    @Schema(name = "requesterUserId", description = "请求人筛选")
    @JsonProperty(value = "requesterUserId")
    private Long requesterUserId;

    @Schema(name = "periodStart", description = "统计起始时间")
    @JsonProperty(value = "periodStart")
    private Instant periodStart;

    @Schema(name = "periodEnd", description = "统计结束时间")
    @JsonProperty(value = "periodEnd")
    private Instant periodEnd;
}
