package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsReportDetailRequest", description = "Operations 报表详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsReportDetailRequest {

    @NotNull
    @Schema(name = "reportId", description = "报表任务标识")
    @JsonProperty(value = "reportId")
    private Long reportId;
}
