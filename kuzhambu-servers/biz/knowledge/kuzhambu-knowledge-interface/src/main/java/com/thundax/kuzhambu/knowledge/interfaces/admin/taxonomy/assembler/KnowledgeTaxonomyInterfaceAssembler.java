package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.assembler;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagBatchReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCandidateApplyItem;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagExtractionCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagBatchMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagBatchMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagExtractionResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasRemoveRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchMergeRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchReviewRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest.TagCandidateApplyItemRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryStatusRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryUpdateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagExtractionRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagGovernanceMetricsRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagMergeRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagReviewPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagReviewRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagStatusRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagUpdateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagAliasResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagBatchMergePreviewResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagCategoryResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagContentRefResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagDetailResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagExtractionResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagGovernanceMetricsResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagMergePreviewResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagResponse;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class KnowledgeTaxonomyInterfaceAssembler {

    private KnowledgeTaxonomyInterfaceAssembler() {}

    @NonNull
    public static TagCategoryQuery toQuery(@NonNull TagCategoryPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCategoryQuery(
                request.getName(),
                StringUtils.isBlank(request.getStatus()) ? null : TagCategoryStatus.from(request.getStatus()),
                resolveSortDirection(request.getSortDirection()));
    }

    @NonNull
    public static TagCategoryCreateCommand toCategoryCreateCommand(@NonNull TagCategoryCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCategoryCreateCommand(
                TagCategoryIdCodec.toDomain(request.getId()),
                request.getName(),
                request.getDescription(),
                request.getStatus() == null ? TagCategoryStatus.ENABLED : TagCategoryStatus.from(request.getStatus()));
    }

    @NonNull
    public static TagCategoryUpdateCommand toCategoryUpdateCommand(@NonNull TagCategoryUpdateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCategoryUpdateCommand(
                TagCategoryIdCodec.toDomain(request.getId()), request.getName(), request.getDescription());
    }

    @NonNull
    public static TagCategoryStatusCommand toCategoryStatusCommand(@NonNull TagCategoryStatusRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCategoryStatusCommand(
                TagCategoryIdCodec.toDomain(request.getId()), TagCategoryStatus.from(request.getStatus()));
    }

    @NonNull
    public static TagQuery toQuery(@NonNull TagPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagQuery(
                request.getName(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                StringUtils.isBlank(request.getStatus()) ? null : TagStatus.from(request.getStatus()),
                StringUtils.isBlank(request.getSource()) ? null : TagSource.from(request.getSource()),
                StringUtils.isBlank(request.getReviewStatus()) ? null : TagReviewStatus.from(request.getReviewStatus()),
                resolveSortDirection(request.getSortDirection()));
    }

    @NonNull
    public static TagCreateCommand toCreateCommand(@NonNull TagCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCreateCommand(
                TagIdCodec.toDomain(request.getId()),
                request.getName(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                request.getDescription(),
                StringUtils.isBlank(request.getReviewStatus()) ? null : TagReviewStatus.from(request.getReviewStatus()),
                request.getReviewNote(),
                request.getReviewedAt());
    }

    @NonNull
    public static TagUpdateCommand toUpdateCommand(@NonNull TagUpdateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagUpdateCommand(
                TagIdCodec.toDomain(request.getId()),
                request.getName(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                request.getDescription());
    }

    @NonNull
    public static TagStatusCommand toStatusCommand(@NonNull TagStatusRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagStatusCommand(TagIdCodec.toDomain(request.getId()), TagStatus.from(request.getStatus()));
    }

    @NonNull
    public static TagMergePreviewQuery toMergePreviewQuery(@NonNull TagMergeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagMergePreviewQuery(
                TagIdCodec.toDomain(request.getSourceTagId()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    @NonNull
    public static TagMergeCommand toMergeCommand(@NonNull TagMergeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagMergeCommand(
                TagIdCodec.toDomain(request.getSourceTagId()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    @NonNull
    public static TagBatchMergePreviewQuery toBatchMergePreviewQuery(@NonNull TagBatchMergeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagBatchMergePreviewQuery(
                toTagIds(request.getSourceTagIds()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    @NonNull
    public static TagBatchMergeCommand toBatchMergeCommand(@NonNull TagBatchMergeRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagBatchMergeCommand(
                toTagIds(request.getSourceTagIds()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    @NonNull
    public static TagDeprecateCommand toDeprecateCommand(@NonNull TagDeprecateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagDeprecateCommand(TagIdCodec.toDomain(request.getId()));
    }

    @NonNull
    public static TagBatchDeprecateCommand toBatchDeprecateCommand(@NonNull TagBatchDeprecateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagBatchDeprecateCommand(toTagIds(request.getTagIds()));
    }

    @NonNull
    public static TagGovernanceMetricsQuery toMetricsQuery(@NonNull TagGovernanceMetricsRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagGovernanceMetricsQuery(request.getTopLimit(), request.getRecentMonths());
    }

    @NonNull
    public static TagReviewQuery toQuery(@NonNull TagReviewPageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagReviewQuery(
                request.getName(),
                StringUtils.isBlank(request.getSource()) ? null : TagSource.from(request.getSource()),
                resolveSortDirection(request.getSortDirection()));
    }

    @NonNull
    public static TagReviewCommand toReviewCommand(@NonNull TagReviewRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagReviewCommand(
                TagIdCodec.toDomain(request.getId()), request.getDecision(), request.getReviewNote());
    }

    @NonNull
    public static TagBatchReviewCommand toBatchReviewCommand(@NonNull TagBatchReviewRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagBatchReviewCommand(
                toTagIds(request.getTagIds()),
                request.getDecision(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                request.getReviewNote());
    }

    @NonNull
    public static TagExtractionCommand toExtractionCommand(@NonNull TagExtractionRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagExtractionCommand(
                request.getSourceContentType(),
                request.getSourceContentId(),
                request.getContentTitle(),
                request.getContentText(),
                request.getModelId(),
                request.getModelName(),
                request.getPromptVersionId(),
                request.getMaxTags(),
                request.getAllowNewTags(),
                request.getRequestedBy());
    }

    @NonNull
    public static TagCandidateApplyCommand toCandidateApplyCommand(@NonNull TagCandidateApplyRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagCandidateApplyCommand(
                request.getAiCandidateId(),
                request.getSelectedTags() == null
                        ? null
                        : request.getSelectedTags().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toCandidateApplyItemCommand)
                                .toList(),
                request.getReviewNote(),
                request.getReviewedBy());
    }

    private static TagCandidateApplyItem toCandidateApplyItemCommand(TagCandidateApplyItemRequest request) {
        return new TagCandidateApplyItem(
                request == null ? null : request.getName(),
                request == null ? null : request.getCategoryId(),
                request == null ? null : request.getCategoryName(),
                request == null ? null : request.getConfidence(),
                request == null ? null : request.getReason(),
                request == null ? null : request.getMatchedExistingTagId());
    }

    @NonNull
    public static TagAliasCreateCommand toAliasCreateCommand(@NonNull TagAliasCreateRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagAliasCreateCommand(
                TagAliasIdCodec.toDomain(request.getId()),
                TagIdCodec.toDomain(request.getTagId()),
                request.getName(),
                StringUtils.isBlank(request.getSource()) ? null : TagSource.from(request.getSource()));
    }

    @NonNull
    public static TagAliasRemoveCommand toAliasRemoveCommand(@NonNull TagAliasRemoveRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        return new TagAliasRemoveCommand(TagAliasIdCodec.toDomain(request.getId()));
    }

    @NonNull
    public static TagCategoryResponse toResponse(@NonNull TagCategoryResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagCategoryResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .description(result.getDescription())
                .status(result.getStatus())
                .build();
    }

    @NonNull
    public static TagResponse toResponse(@NonNull TagResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .categoryId(result.getCategoryId())
                .categoryName(result.getCategoryName())
                .description(result.getDescription())
                .status(result.getStatus())
                .source(result.getSource())
                .reviewStatus(result.getReviewStatus())
                .contentRefCount(result.getContentRefCount())
                .createdAt(result.getCreatedAt())
                .reviewedAt(result.getReviewedAt())
                .build();
    }

    @NonNull
    public static TagDetailResponse toResponse(@NonNull TagDetailResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagDetailResponse.builder()
                .tag(result.getTag() == null ? null : toResponse(result.getTag()))
                .aliases(
                        result.getAliases() == null
                                ? null
                                : result.getAliases().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .contentRefs(
                        result.getContentRefs() == null
                                ? null
                                : result.getContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    @NonNull
    public static TagAliasResponse toResponse(@NonNull TagAliasResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagAliasResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .source(result.getSource())
                .build();
    }

    @NonNull
    public static TagContentRefResponse toResponse(@NonNull TagContentRefResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagContentRefResponse.builder()
                .id(result.getId())
                .contentType(result.getContentType())
                .contentId(result.getContentId())
                .contentTitle(result.getContentTitle())
                .source(result.getSource())
                .build();
    }

    @NonNull
    public static TagMergePreviewResponse toResponse(@NonNull TagMergePreviewResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagMergePreviewResponse.builder()
                .sourceTag(result.getSourceTag() == null ? null : toResponse(result.getSourceTag()))
                .targetTag(result.getTargetTag() == null ? null : toResponse(result.getTargetTag()))
                .aliasesToMerge(
                        result.getAliasesToMerge() == null
                                ? null
                                : result.getAliasesToMerge().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .impactedContentRefs(
                        result.getImpactedContentRefs() == null
                                ? null
                                : result.getImpactedContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .pendingReviewCount(result.getPendingReviewCount())
                .governedRecordCount(result.getGovernedRecordCount())
                .build();
    }

    @NonNull
    public static TagBatchMergePreviewResponse toResponse(@NonNull TagBatchMergePreviewResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagBatchMergePreviewResponse.builder()
                .sourceTags(
                        result.getSourceTags() == null
                                ? null
                                : result.getSourceTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .targetTag(result.getTargetTag() == null ? null : toResponse(result.getTargetTag()))
                .aliasesToMerge(
                        result.getAliasesToMerge() == null
                                ? null
                                : result.getAliasesToMerge().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .impactedContentRefs(
                        result.getImpactedContentRefs() == null
                                ? null
                                : result.getImpactedContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .pendingReviewCount(result.getPendingReviewCount())
                .governedRecordCount(result.getGovernedRecordCount())
                .build();
    }

    @NonNull
    public static TagGovernanceMetricsResponse toResponse(@NonNull TagGovernanceMetricsResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagGovernanceMetricsResponse.builder()
                .topTags(
                        result.getTopTags() == null
                                ? null
                                : result.getTopTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .categoryDistributions(
                        result.getCategoryDistributions() == null
                                ? null
                                : result.getCategoryDistributions().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .sourceRatios(
                        result.getSourceRatios() == null
                                ? null
                                : result.getSourceRatios().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .monthlyNewTags(
                        result.getMonthlyNewTags() == null
                                ? null
                                : result.getMonthlyNewTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    @NonNull
    public static TagExtractionResponse toResponse(@NonNull TagExtractionResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagExtractionResponse.builder()
                .aiCallId(result.getAiCallId())
                .aiCandidateId(result.getAiCandidateId())
                .status(result.getStatus())
                .resultFormat(result.getResultFormat())
                .resultPayload(result.getResultPayload())
                .errorType(result.getErrorType())
                .errorMessage(result.getErrorMessage())
                .build();
    }

    @NonNull
    public static TagGovernanceMetricsResponse.TagUsageMetric toResponse(
            @NonNull TagGovernanceMetricsResult.TagUsageMetric result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagGovernanceMetricsResponse.TagUsageMetric.builder()
                .tagName(result.getTagName())
                .contentRefCount(result.getContentRefCount())
                .build();
    }

    @NonNull
    public static TagGovernanceMetricsResponse.CategoryDistributionMetric toResponse(
            @NonNull TagGovernanceMetricsResult.CategoryDistributionMetric result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagGovernanceMetricsResponse.CategoryDistributionMetric.builder()
                .categoryName(result.getCategoryName())
                .tagCount(result.getTagCount())
                .build();
    }

    @NonNull
    public static TagGovernanceMetricsResponse.SourceRatioMetric toResponse(
            @NonNull TagGovernanceMetricsResult.SourceRatioMetric result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagGovernanceMetricsResponse.SourceRatioMetric.builder()
                .source(result.getSource() == null ? null : result.getSource().value())
                .tagCount(result.getTagCount())
                .build();
    }

    @NonNull
    public static TagGovernanceMetricsResponse.MonthlyNewTagMetric toResponse(
            @NonNull TagGovernanceMetricsResult.MonthlyNewTagMetric result) {
        Objects.requireNonNull(result, "result must not be null");
        return TagGovernanceMetricsResponse.MonthlyNewTagMetric.builder()
                .month(result.getMonth())
                .tagCount(result.getTagCount())
                .build();
    }

    private static List<com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId> toTagIds(
            List<String> tagIds) {
        return tagIds == null ? null : tagIds.stream().map(TagIdCodec::toDomain).toList();
    }

    private static SortDirection resolveSortDirection(String sortDirection) {
        return StringUtils.isBlank(sortDirection)
                ? SortDirection.ASC
                : SortDirection.valueOf(sortDirection.trim().toUpperCase());
    }
}
