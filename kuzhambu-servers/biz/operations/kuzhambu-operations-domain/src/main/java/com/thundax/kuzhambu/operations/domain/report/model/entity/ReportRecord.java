package com.thundax.kuzhambu.operations.domain.report.model.entity;

import com.thundax.kuzhambu.operations.domain.report.model.enums.ReportStatus;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRecord {

    private ReportId id;
    private String reportType;
    private String format;
    private Instant periodStart;
    private Instant periodEnd;
    private String requestId;
    private String traceId;
    private String templateVersion;
    private Long storageObjectId;
    private String artifactFilename;
    private ReportStatus reportStatus;
    private String failureReason;
    private Long requesterUserId;
    private Instant requestedAt;
    private Instant completedAt;
}
