package com.thundax.kuzhambu.knowledge.application.refinement.service;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportPageQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;

public interface KnowledgeQualityReportApplicationService {

    QualityReportDetailResult generateReport(GenerateQualityReportCommand command);

    PageResult<ReportRecord> pageReports(QualityReportPageQuery query);

    QualityReportDetailResult detail(Long reportId);

    QualityReportDetailResult latest(Long graphVersionId);

    ReextractLowQualityCategoryResult reextractLowQualityCategory(ReextractLowQualityCategoryCommand command);
}
