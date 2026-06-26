package com.thundax.kuzhambu.operations.interfaces.admin.report.controller.response;

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
public class OperationsReportDetailResponse {
    private Long reportId;
    private String reportType;
    private String format;
    private Date periodStart;
    private Date periodEnd;
    private String requestId;
    private String traceId;
    private String templateVersion;
    private Long storageObjectId;
    private String artifactFilename;
    private String reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;
}
