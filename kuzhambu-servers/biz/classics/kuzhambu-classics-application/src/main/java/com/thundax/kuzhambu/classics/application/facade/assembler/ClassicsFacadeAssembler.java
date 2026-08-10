package com.thundax.kuzhambu.classics.application.facade.assembler;

import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsContentGrowthPointFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsTopContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
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

    public ClassicsQaKnowledgeFacadeResponse toQaKnowledgeFacadeResponse(ClassicsQaKnowledgeFacadeDto knowledge) {
        return ClassicsQaKnowledgeFacadeResponse.builder().knowledge(knowledge).build();
    }

    public ClassicsQaKnowledgeFacadeDto toQaKnowledgeFacadeDto(
            ClassicsSearchSourceContent sourceContent,
            String originalText,
            String translationText,
            String body,
            String originalExcerpts,
            List<ClassicsContentTag> tags,
            List<ClassicsContentQaPair> qaPairs) {
        if (sourceContent == null) {
            return null;
        }
        return ClassicsQaKnowledgeFacadeDto.builder()
                .sourceId(sourceContent.getContentType() + ":" + sourceContent.getContentId())
                .contentType(sourceContent.getContentType())
                .contentId(sourceContent.getContentId())
                .knowledgeBase(sourceContent.getKnowledgeBase())
                .currentVersionNo(sourceContent.getCurrentVersionNo())
                .knowledgeRevision(null)
                .visibility(sourceContent.getVisibility())
                .status(sourceContent.getStatus())
                .sourcePath(buildSourcePath(sourceContent.getContentType(), sourceContent.getContentId()))
                .updatedAt(sourceContent.getUpdatedAt())
                .title(sourceContent.getTitle())
                .categoryPath(sourceContent.getCategoryName())
                .summary(sourceContent.getSummary())
                .body(body)
                .originalText(originalText)
                .translationText(translationText)
                .originalExcerpts(originalExcerpts)
                .tags(toTagNames(tags))
                .qaPairs(toQaPairs(qaPairs))
                .build();
    }

    @NonNull
    public ContentObjectQuery toContentObjectQuery(@NonNull String contentType, @NonNull ClassicsContentId contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return new ContentObjectQuery(contentType, contentId);
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
                .volumeCode(content.getVolumeCode())
                .volumeName(content.getVolumeName())
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

    private String buildSourcePath(String contentType, String contentId) {
        return switch (contentType) {
            case "SANCAI_ENTRY" -> "/classics/sancai/" + contentId;
            case "WANGQI_DOCUMENT" -> "/classics/wangqi/" + contentId;
            case "MING_CUSTOMS" -> "/classics/ming-customs/" + contentId;
            default -> "/classics/unknown/" + contentId;
        };
    }

    private List<String> toTagNames(List<ClassicsContentTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream()
                .filter(tag -> tag != null
                        && (tag.getStatus() == null || tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                        && StringUtils.isNotBlank(tag.getTagNameSnapshot()))
                .map(ClassicsContentTag::getTagNameSnapshot)
                .toList();
    }

    private List<ClassicsQaKnowledgeFacadeDto.QaPair> toQaPairs(List<ClassicsContentQaPair> qaPairs) {
        if (qaPairs == null || qaPairs.isEmpty()) {
            return Collections.emptyList();
        }
        return qaPairs.stream()
                .filter(pair -> pair != null
                        && pair.getSource() != null
                        && (pair.getSource() == ClassicsContentSource.MANUAL
                                || pair.getSource() == ClassicsContentSource.AI)
                        && StringUtils.isNotBlank(pair.getQuestion())
                        && StringUtils.isNotBlank(pair.getAnswer()))
                .map(pair -> ClassicsQaKnowledgeFacadeDto.QaPair.builder()
                        .question(pair.getQuestion())
                        .answer(pair.getAnswer())
                        .build())
                .toList();
    }
}
