package com.thundax.kuzhambu.classics.application.facade.assembler;

import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicsFacadeAssembler {

    public ClassicsSummaryFacadeResponse toFacadeResponse(ClassicsReportSummaryResult result) {
        if (result == null) {
            return null;
        }
        return ClassicsSummaryFacadeResponse.builder()
                .periodStart(result.getPeriodStart())
                .periodEnd(result.getPeriodEnd())
                .contentCount(result.getContentCount())
                .translatedContentCount(result.getTranslatedContentCount())
                .imageReadyContentCount(result.getImageReadyContentCount())
                .visualAssetReadyContentCount(result.getVisualAssetReadyContentCount())
                .shareVisitCount(result.getShareVisitCount())
                .topContents(toTopContentFacadeDtos(result.getTopContents()))
                .contentGrowthSeries(toContentGrowthPointFacadeDtos(result.getContentGrowthSeries()))
                .build();
    }

    public ClassicsPublicContentsFacadeResponse toPublicContentsFacadeResponse(
            List<ClassicsSearchSourceContent> contents) {
        return ClassicsPublicContentsFacadeResponse.builder()
                .contents(toPublicContentFacadeDtos(contents))
                .build();
    }

    public ClassicsPublicContentFacadeResponse toPublicContentFacadeResponse(ClassicsSearchSourceContent content) {
        return ClassicsPublicContentFacadeResponse.builder()
                .content(toPublicContentFacadeDto(content))
                .build();
    }

    private List<ClassicsTopContentFacadeDto> toTopContentFacadeDtos(
            List<ClassicsReportSummaryResult.TopContentResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> ClassicsTopContentFacadeDto.builder()
                        .contentId(result.getContentId())
                        .contentType(result.getContentType())
                        .title(result.getTitle())
                        .visitCount(result.getVisitCount())
                        .build())
                .toList();
    }

    private List<ClassicsContentGrowthPointFacadeDto> toContentGrowthPointFacadeDtos(
            List<ClassicsReportSummaryResult.ContentGrowthPointResult> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(result -> ClassicsContentGrowthPointFacadeDto.builder()
                        .bucket(result.getBucket())
                        .createdCount(result.getCreatedCount())
                        .build())
                .toList();
    }

    private List<ClassicsPublicContentFacadeDto> toPublicContentFacadeDtos(List<ClassicsSearchSourceContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        return contents.stream().map(this::toPublicContentFacadeDto).toList();
    }

    private ClassicsPublicContentFacadeDto toPublicContentFacadeDto(ClassicsSearchSourceContent content) {
        if (content == null) {
            return null;
        }
        return ClassicsPublicContentFacadeDto.builder()
                .contentType(content.getContentType())
                .contentId(content.getContentId())
                .knowledgeBase(content.getKnowledgeBase())
                .categoryCode(content.getCategoryCode())
                .categoryName(content.getCategoryName())
                .title(content.getTitle())
                .summary(content.getSummary())
                .textSegments(content.getTextSegments())
                .tagNames(content.getTagNames())
                .status(content.getStatus())
                .visibility(content.getVisibility())
                .currentVersionNo(content.getCurrentVersionNo())
                .publishedAt(content.getPublishedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
}
