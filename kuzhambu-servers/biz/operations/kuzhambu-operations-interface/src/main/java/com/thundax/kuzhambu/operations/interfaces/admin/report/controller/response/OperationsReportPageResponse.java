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
public class OperationsReportPageResponse {
    private Long reportId;
    private String reportType;
    private String format;
    private Date periodStart;
    private Date periodEnd;
    private Long storageObjectId;
    private String artifactFilename;
    private String reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Date requestedAt;
    private Date completedAt;
}
