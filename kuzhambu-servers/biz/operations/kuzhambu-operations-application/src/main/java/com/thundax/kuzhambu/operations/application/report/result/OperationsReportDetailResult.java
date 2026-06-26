package com.thundax.kuzhambu.operations.application.report.result;

import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportDetailResult {

    private ReportId reportId;
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
