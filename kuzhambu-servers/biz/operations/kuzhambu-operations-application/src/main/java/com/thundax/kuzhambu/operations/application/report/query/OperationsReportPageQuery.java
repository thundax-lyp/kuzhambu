package com.thundax.kuzhambu.operations.application.report.query;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportPageQuery {

    private String reportType;
    private String format;
    private String reportStatus;
    private Long requesterUserId;
    private Date periodStart;
    private Date periodEnd;
}
