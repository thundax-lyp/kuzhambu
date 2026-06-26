package com.thundax.kuzhambu.operations.application.report.result;

import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportGenerateResult {

    private ReportId reportId;
    private String reportStatus;
}
