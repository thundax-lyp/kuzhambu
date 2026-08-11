package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsReportPageResponse", description = "Operations 报表分页响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsReportPageResponse {
    private Long reportId;
    private String reportType;
    private String format;
    private Instant periodStart;
    private Instant periodEnd;
    private Long storageObjectId;
    private String artifactFilename;
    private String reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Instant requestedAt;
    private Instant completedAt;
}
