package com.thundax.kuzhambu.knowledge.application.refinement.service;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.application.refinement.command.GenerateQualityReportCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.command.ReextractLowQualityCategoryCommand;
import com.thundax.kuzhambu.knowledge.application.refinement.query.LatestQualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportDetailQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.query.QualityReportQuery;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult;
import com.thundax.kuzhambu.knowledge.application.refinement.result.QualityReportDetailResult.ReportRecord;
import com.thundax.kuzhambu.knowledge.application.refinement.result.ReextractLowQualityCategoryResult;

public interface KnowledgeQualityReportApplicationService {

    QualityReportDetailResult generateReport(GenerateQualityReportCommand command);

    PageResult<ReportRecord> pageReports(QualityReportQuery query, PageQuery pageQuery);

    QualityReportDetailResult detail(QualityReportDetailQuery query);

    QualityReportDetailResult latest(LatestQualityReportQuery query);

    ReextractLowQualityCategoryResult reextractLowQualityCategory(ReextractLowQualityCategoryCommand command);
}
