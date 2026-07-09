package com.thundax.kuzhambu.operations.application.report.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDownloadResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;

public interface ReportApplicationService {

    OperationsReportGenerateResult generate(OperationsReportGenerateCommand command);

    PageResult<OperationsReportPageResult> page(OperationsReportPageQuery query, PageQuery pageQuery);

    OperationsReportDetailResult detail(OperationsReportDetailQuery query);

    OperationsReportDownloadResult download(OperationsReportDetailQuery query);
}
