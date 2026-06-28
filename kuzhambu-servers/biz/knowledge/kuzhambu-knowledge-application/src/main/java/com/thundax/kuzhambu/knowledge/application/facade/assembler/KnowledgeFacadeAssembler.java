package com.thundax.kuzhambu.knowledge.application.facade.assembler;

import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.CategoryDistributionResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.MonthlyNewTagResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.TopTagResult;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeCategoryDistributionFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeMonthlyNewTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeTopTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeFacadeAssembler {

    public KnowledgeSummaryFacadeResponse toSummaryResponse(KnowledgeReportSummaryResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgeSummaryFacadeResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .tagCoverageRate(result.getTagCoverageRate())
                .topTags(toTopTagFacadeDtos(result.getTopTags()))
                .categoryDistributions(toCategoryDistributionFacadeDtos(result.getCategoryDistributions()))
                .monthlyNewTags(toMonthlyNewTagFacadeDtos(result.getMonthlyNewTags()))
                .build();
    }

    private List<KnowledgeTopTagFacadeDto> toTopTagFacadeDtos(List<TopTagResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> KnowledgeTopTagFacadeDto.builder()
                        .tagName(result.getTagName())
                        .contentRefCount(result.getContentRefCount())
                        .build())
                .toList();
    }

    private List<KnowledgeCategoryDistributionFacadeDto> toCategoryDistributionFacadeDtos(
            List<CategoryDistributionResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> KnowledgeCategoryDistributionFacadeDto.builder()
                        .categoryName(result.getCategoryName())
                        .tagCount(result.getTagCount())
                        .build())
                .toList();
    }

    private List<KnowledgeMonthlyNewTagFacadeDto> toMonthlyNewTagFacadeDtos(List<MonthlyNewTagResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> KnowledgeMonthlyNewTagFacadeDto.builder()
                        .bucket(result.getBucket())
                        .tagCount(result.getTagCount())
                        .build())
                .toList();
    }
}
