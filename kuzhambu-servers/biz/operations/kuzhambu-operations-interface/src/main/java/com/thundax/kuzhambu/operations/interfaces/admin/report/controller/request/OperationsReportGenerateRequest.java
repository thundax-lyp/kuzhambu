package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsReportGenerateRequest", description = "Operations 报表生成请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsReportGenerateRequest {

    @NotBlank
    @Schema(name = "reportType", description = "报表类型")
    @JsonProperty(value = "reportType")
    private String reportType;

    @NotBlank
    @Schema(name = "format", description = "导出格式")
    @JsonProperty(value = "format")
    private String format;

    @NotNull
    @Schema(name = "periodStart", description = "统计起始时间")
    @JsonProperty(value = "periodStart")
    private Instant periodStart;

    @NotNull
    @Schema(name = "periodEnd", description = "统计结束时间")
    @JsonProperty(value = "periodEnd")
    private Instant periodEnd;
}
