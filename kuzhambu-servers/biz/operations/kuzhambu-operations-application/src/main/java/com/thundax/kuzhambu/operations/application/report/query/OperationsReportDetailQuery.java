package com.thundax.kuzhambu.operations.application.report.query;

import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsReportDetailQuery {

    private ReportId reportId;
}
