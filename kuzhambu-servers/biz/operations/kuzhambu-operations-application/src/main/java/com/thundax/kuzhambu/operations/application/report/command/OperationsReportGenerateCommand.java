package com.thundax.kuzhambu.operations.application.report.command;

import java.util.Date;
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
    private Date periodStart;
    private Date periodEnd;
    private Long requesterUserId;
}
