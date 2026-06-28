package com.thundax.kuzhambu.knowledge.application.facade.assembler;

import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.CategoryDistributionResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.MonthlyNewTagResult;
import com.thundax.kuzhambu.knowledge.application.report.result.KnowledgeReportSummaryResult.TopTagResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryEntityHintResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoverySynonymExpandResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.DiscoveryTagHintResult;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeCategoryDistributionFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeEntityHintFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeMonthlyNewTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeTopTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeEntityHintsFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSummaryFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeSynonymExpandFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;
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

    public KnowledgeSynonymExpandFacadeResponse toSynonymExpandResponse(DiscoverySynonymExpandResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgeSynonymExpandFacadeResponse.builder()
                .term(result.getTerm())
                .normalizedTerm(result.getNormalizedTerm())
                .expandedTerms(result.getExpandedTerms())
                .build();
    }

    public KnowledgeTagHintFacadeResponse toTagHintResponse(DiscoveryTagHintResult result) {
        if (result == null) {
            return null;
        }
        return KnowledgeTagHintFacadeResponse.builder()
                .term(result.getTerm())
                .normalizedTerm(result.getNormalizedTerm())
                .matchedTagName(result.getMatchedTagName())
                .matchedAliasName(result.getMatchedAliasName())
                .contentRefCount(result.getContentRefCount())
                .build();
    }

    public KnowledgeEntityHintsFacadeResponse toEntityHintsResponse(List<DiscoveryEntityHintResult> results) {
        return KnowledgeEntityHintsFacadeResponse.builder()
                .entityHints(toEntityHintFacadeDtos(results))
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

    private List<KnowledgeEntityHintFacadeDto> toEntityHintFacadeDtos(List<DiscoveryEntityHintResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> KnowledgeEntityHintFacadeDto.builder()
                        .term(result.getTerm())
                        .normalizedTerm(result.getNormalizedTerm())
                        .entityName(result.getEntityName())
                        .entityType(result.getEntityType())
                        .contentRefCount(result.getContentRefCount())
                        .build())
                .toList();
    }
}
