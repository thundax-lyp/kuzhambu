package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsReportGenerateResponse", description = "Operations 报表生成响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsReportGenerateResponse {
    private Long reportId;
    private String reportStatus;
}
