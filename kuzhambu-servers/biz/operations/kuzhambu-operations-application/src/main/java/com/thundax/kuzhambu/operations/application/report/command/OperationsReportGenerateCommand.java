package com.thundax.kuzhambu.operations.application.report.command;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportGenerateCommand {

    private String reportType;
    private String format;
    private Instant periodStart;
    private Instant periodEnd;
    private Long requesterUserId;
}
