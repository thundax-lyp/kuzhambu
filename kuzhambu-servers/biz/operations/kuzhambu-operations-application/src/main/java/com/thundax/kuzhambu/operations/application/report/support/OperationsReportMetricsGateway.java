package com.thundax.kuzhambu.operations.application.report.support;

import com.thundax.kuzhambu.operations.application.report.support.OperationsReportSupportModels.OperationsReportSection;
import com.thundax.kuzhambu.operations.domain.report.model.entity.ReportRecord;
import java.util.List;

public interface OperationsReportMetricsGateway {

    List<OperationsReportSection> loadSections(ReportRecord record);
}
