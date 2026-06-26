package com.thundax.kuzhambu.knowledge.application.report.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.CategoryDistributionResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.MonthlyNewTagResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.TopTagResult;
import com.thundax.kuzhambu.knowledge.application.report.service.KnowledgeReportApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.readmodel.TagGovernanceMetrics;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.repository.TagGovernanceMetricsRepository;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@BizExceptionBoundary
public class KnowledgeReportApplicationServiceImpl implements KnowledgeReportApplicationService {

    private static final int DEFAULT_TOP_LIMIT = 10;
    private static final int DEFAULT_RECENT_MONTHS = 6;

    private final TagGovernanceMetricsRepository tagGovernanceMetricsRepository;

    public KnowledgeReportApplicationServiceImpl(TagGovernanceMetricsRepository tagGovernanceMetricsRepository) {
        this.tagGovernanceMetricsRepository = tagGovernanceMetricsRepository;
    }

    @Override
    public KnowledgeReportSummaryResult summary(Date periodStart, Date periodEnd, String bucketType) {
        TagGovernanceMetrics metrics =
                tagGovernanceMetricsRepository.getMetrics(DEFAULT_TOP_LIMIT, DEFAULT_RECENT_MONTHS);
        return new KnowledgeReportSummaryResult(
                periodStart,
                periodEnd,
                tagGovernanceMetricsRepository.getTagCoverageRate(),
                mapTopTags(metrics),
                mapCategoryDistributions(metrics),
                mapMonthlyNewTags(metrics));
    }

    private List<TopTagResult> mapTopTags(TagGovernanceMetrics metrics) {
        if (metrics == null || metrics.getTopTags() == null) {
            return List.of();
        }
        return metrics.getTopTags().stream()
                .map(item -> new TopTagResult(item.getTagName(), item.getContentRefCount()))
                .toList();
    }

    private List<CategoryDistributionResult> mapCategoryDistributions(TagGovernanceMetrics metrics) {
        if (metrics == null || metrics.getCategoryDistributions() == null) {
            return List.of();
        }
        return metrics.getCategoryDistributions().stream()
                .map(item -> new CategoryDistributionResult(item.getCategoryName(), item.getTagCount()))
                .toList();
    }

    private List<MonthlyNewTagResult> mapMonthlyNewTags(TagGovernanceMetrics metrics) {
        if (metrics == null || metrics.getMonthlyNewTags() == null) {
            return List.of();
        }
        return metrics.getMonthlyNewTags().stream()
                .map(item -> new MonthlyNewTagResult(item.getMonth(), item.getTagCount()))
                .toList();
    }
}
