package com.thundax.kuzhambu.classics.application.facade.assembler;

import com.thundax.kuzhambu.classics.application.cleanup.command.ClassicsCleanupExecuteCommand;
import com.thundax.kuzhambu.classics.application.cleanup.query.ClassicsCleanupTargetsQuery;
import com.thundax.kuzhambu.classics.application.content.query.ContentObjectQuery;
import com.thundax.kuzhambu.classics.application.report.query.ClassicsReportSummaryQuery;
import com.thundax.kuzhambu.classics.application.report.result.ClassicsReportSummaryResult;
import com.thundax.kuzhambu.classics.application.search.query.ClassicsSearchContentQuery;
import com.thundax.kuzhambu.classics.application.search.query.ClassicsWorkbenchContentQuery;
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
import com.thundax.kuzhambu.classics.facade.request.ClassicsCleanupTargetsFacadeRequest;
import com.thundax.kuzhambu.classics.facade.request.ClassicsSummaryFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsSummaryFacadeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ClassicsFacadeAssembler {

    @NonNull
    public ClassicsSummaryFacadeResponse toFacadeResponse(@NonNull ClassicsReportSummaryResult result) {
        Objects.requireNonNull(result, "result");
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

    @NonNull
    public ClassicsPublicContentsFacadeResponse toPublicContentsFacadeResponse(
            @NonNull List<ClassicsSearchSourceContent> contents) {
        Objects.requireNonNull(contents, "contents");
        return ClassicsPublicContentsFacadeResponse.builder()
                .contents(toPublicContentFacadeDtos(contents))
                .build();
    }

    @NonNull
    public ClassicsPublicContentFacadeResponse toPublicContentFacadeResponse(
            @NonNull ClassicsSearchSourceContent content) {
        Objects.requireNonNull(content, "content");
        return ClassicsPublicContentFacadeResponse.builder()
                .content(toPublicContentFacadeDto(content))
                .build();
    }

    @NonNull
    public ClassicsQaKnowledgeFacadeResponse toQaKnowledgeFacadeResponse(
            @NonNull ClassicsQaKnowledgeFacadeDto knowledge) {
        Objects.requireNonNull(knowledge, "knowledge");
        return ClassicsQaKnowledgeFacadeResponse.builder().knowledge(knowledge).build();
    }

    @NonNull
    public ClassicsReportSummaryQuery toReportSummaryQuery(@NonNull ClassicsSummaryFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new ClassicsReportSummaryQuery(
                request.getPeriodStart(), request.getPeriodEnd(), request.getBucketType());
    }

    @NonNull
    public ClassicsSearchContentQuery toSearchContentQuery(@NonNull String contentType, @NonNull String contentId) {
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(contentId, "contentId");
        return new ClassicsSearchContentQuery(contentType, contentId);
    }

    @NonNull
    public ClassicsWorkbenchContentQuery toWorkbenchContentQuery(
            @NonNull String categoryCode, @NonNull String volumeCode) {
        Objects.requireNonNull(categoryCode, "categoryCode");
        Objects.requireNonNull(volumeCode, "volumeCode");
        return new ClassicsWorkbenchContentQuery(categoryCode, volumeCode);
    }

    @NonNull
    public ClassicsCleanupTargetsQuery toCleanupTargetsQuery(@NonNull ClassicsCleanupTargetsFacadeRequest request) {
        Objects.requireNonNull(request, "request");
        return new ClassicsCleanupTargetsQuery(
                normalizeCleanupType(request.getCleanupType()),
                request.getRequestedAt(),
                request.getRetentionDays(),
                request.getLimit());
    }

    private String normalizeCleanupType(String cleanupType) {
        return cleanupType == null ? null : cleanupType.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeOptionalText(String value) {
        return value.isBlank() ? null : value;
    }

    @NonNull
    public ClassicsCleanupExecuteCommand toCleanupExecuteCommand(@NonNull String cleanupType, @NonNull Long targetId) {
        Objects.requireNonNull(cleanupType, "cleanupType");
        Objects.requireNonNull(targetId, "targetId");
        return new ClassicsCleanupExecuteCommand(cleanupType, targetId);
    }

    @NonNull
    public ClassicsQaKnowledgeFacadeDto toQaKnowledgeFacadeDto(
            @NonNull ClassicsSearchSourceContent sourceContent,
            @NonNull String originalText,
            @NonNull String translationText,
            @NonNull String body,
            @NonNull String originalExcerpts,
            @NonNull List<ClassicsContentTag> tags,
            @NonNull List<ClassicsContentQaPair> qaPairs) {
        Objects.requireNonNull(sourceContent, "sourceContent");
        Objects.requireNonNull(originalText, "originalText");
        Objects.requireNonNull(translationText, "translationText");
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(originalExcerpts, "originalExcerpts");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(qaPairs, "qaPairs");
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
                .body(normalizeOptionalText(body))
                .originalText(normalizeOptionalText(originalText))
                .translationText(normalizeOptionalText(translationText))
                .originalExcerpts(normalizeOptionalText(originalExcerpts))
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
