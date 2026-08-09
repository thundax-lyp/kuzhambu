package com.thundax.kuzhambu.operations.application.report.query;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportQuery {

    private String reportType;
    private String format;
    private String reportStatus;
    private Long requesterUserId;
    private Instant periodStart;
    private Instant periodEnd;
}
