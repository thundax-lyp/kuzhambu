package com.thundax.kuzhambu.operations.application.report.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.operations.application.report.command.OperationsReportGenerateCommand;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportDetailQuery;
import com.thundax.kuzhambu.operations.application.report.query.OperationsReportPageQuery;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportDetailResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportGenerateResult;
import com.thundax.kuzhambu.operations.application.report.result.OperationsReportPageResult;
import com.thundax.kuzhambu.operations.application.report.service.ReportApplicationService;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class ReportApplicationServiceImpl implements ReportApplicationService {

    @Override
    public OperationsReportGenerateResult generate(OperationsReportGenerateCommand command) {
        throw new UnsupportedOperationException("Operations report generate flow is not implemented yet.");
    }

    @Override
    public PageResult<OperationsReportPageResult> page(OperationsReportPageQuery query, PageQuery pageQuery) {
        throw new UnsupportedOperationException("Operations report page flow is not implemented yet.");
    }

    @Override
    public OperationsReportDetailResult detail(OperationsReportDetailQuery query) {
        throw new UnsupportedOperationException("Operations report detail flow is not implemented yet.");
    }
}
