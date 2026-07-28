package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.report.result.OperationsReportArtifactResult;
import com.thundax.kuzhambu.operations.domain.report.model.valueobject.ReportId;

public interface OperationsReportTaskExecutor {

    void executeAsync(ReportId reportId);

    OperationsReportArtifactResult execute(ReportId reportId);
}
