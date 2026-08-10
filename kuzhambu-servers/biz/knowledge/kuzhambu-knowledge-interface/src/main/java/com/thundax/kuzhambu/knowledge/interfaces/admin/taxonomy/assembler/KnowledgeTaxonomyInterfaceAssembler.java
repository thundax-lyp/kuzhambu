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
import org.apache.commons.lang3.StringUtils;

public final class KnowledgeTaxonomyInterfaceAssembler {

    private KnowledgeTaxonomyInterfaceAssembler() {}

    public static TagCategoryQuery toQuery(TagCategoryPageRequest request) {
        return new TagCategoryQuery(
                request == null ? null : request.getName(),
                request == null || StringUtils.isBlank(request.getStatus())
                        ? null
                        : TagCategoryStatus.from(request.getStatus()),
                resolveSortDirection(request == null ? null : request.getSortDirection()));
    }

    public static TagCategoryCreateCommand toCategoryCreateCommand(TagCategoryCreateRequest request) {
        return new TagCategoryCreateCommand(
                TagCategoryIdCodec.toDomain(request.getId()),
                request.getName(),
                request.getDescription(),
                request.getStatus() == null ? TagCategoryStatus.ENABLED : TagCategoryStatus.from(request.getStatus()));
    }

    public static TagCategoryUpdateCommand toCategoryUpdateCommand(TagCategoryUpdateRequest request) {
        return new TagCategoryUpdateCommand(
                TagCategoryIdCodec.toDomain(request.getId()), request.getName(), request.getDescription());
    }

    public static TagCategoryStatusCommand toCategoryStatusCommand(TagCategoryStatusRequest request) {
        return new TagCategoryStatusCommand(
                TagCategoryIdCodec.toDomain(request.getId()), TagCategoryStatus.from(request.getStatus()));
    }

    public static TagQuery toQuery(TagPageRequest request) {
        return new TagQuery(
                request == null ? null : request.getName(),
                TagCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()),
                request == null || StringUtils.isBlank(request.getStatus())
                        ? null
                        : TagStatus.from(request.getStatus()),
                request == null || StringUtils.isBlank(request.getSource())
                        ? null
                        : TagSource.from(request.getSource()),
                request == null || StringUtils.isBlank(request.getReviewStatus())
                        ? null
                        : TagReviewStatus.from(request.getReviewStatus()),
                resolveSortDirection(request == null ? null : request.getSortDirection()));
    }

    public static TagCreateCommand toCreateCommand(TagCreateRequest request) {
        return new TagCreateCommand(
                TagIdCodec.toDomain(request.getId()),
                request.getName(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                request.getDescription(),
                StringUtils.isBlank(request.getReviewStatus()) ? null : TagReviewStatus.from(request.getReviewStatus()),
                request.getReviewNote(),
                request.getReviewedAt());
    }

    public static TagUpdateCommand toUpdateCommand(TagUpdateRequest request) {
        return new TagUpdateCommand(
                TagIdCodec.toDomain(request.getId()),
                request.getName(),
                TagCategoryIdCodec.toDomain(request.getCategoryId()),
                request.getDescription());
    }

    public static TagStatusCommand toStatusCommand(TagStatusRequest request) {
        return new TagStatusCommand(TagIdCodec.toDomain(request.getId()), TagStatus.from(request.getStatus()));
    }

    public static TagMergePreviewQuery toMergePreviewQuery(TagMergeRequest request) {
        return new TagMergePreviewQuery(
                TagIdCodec.toDomain(request.getSourceTagId()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    public static TagMergeCommand toMergeCommand(TagMergeRequest request) {
        return new TagMergeCommand(
                TagIdCodec.toDomain(request.getSourceTagId()), TagIdCodec.toDomain(request.getTargetTagId()));
    }

    public static TagBatchMergePreviewQuery toBatchMergePreviewQuery(TagBatchMergeRequest request) {
        return new TagBatchMergePreviewQuery(
                toTagIds(request == null ? null : request.getSourceTagIds()),
                TagIdCodec.toDomain(request == null ? null : request.getTargetTagId()));
    }

    public static TagBatchMergeCommand toBatchMergeCommand(TagBatchMergeRequest request) {
        return new TagBatchMergeCommand(
                toTagIds(request == null ? null : request.getSourceTagIds()),
                TagIdCodec.toDomain(request == null ? null : request.getTargetTagId()));
    }

    public static TagDeprecateCommand toDeprecateCommand(TagDeprecateRequest request) {
        return new TagDeprecateCommand(TagIdCodec.toDomain(request.getId()));
    }

    public static TagBatchDeprecateCommand toBatchDeprecateCommand(TagBatchDeprecateRequest request) {
        return new TagBatchDeprecateCommand(toTagIds(request == null ? null : request.getTagIds()));
    }

    public static TagGovernanceMetricsQuery toMetricsQuery(TagGovernanceMetricsRequest request) {
        return new TagGovernanceMetricsQuery(
                request == null ? null : request.getTopLimit(), request == null ? null : request.getRecentMonths());
    }

    public static TagReviewQuery toQuery(TagReviewPageRequest request) {
        return new TagReviewQuery(
                request == null ? null : request.getName(),
                request == null || StringUtils.isBlank(request.getSource())
                        ? null
                        : TagSource.from(request.getSource()),
                resolveSortDirection(request == null ? null : request.getSortDirection()));
    }

    public static TagReviewCommand toReviewCommand(TagReviewRequest request) {
        return new TagReviewCommand(
                TagIdCodec.toDomain(request.getId()), request.getDecision(), request.getReviewNote());
    }

    public static TagBatchReviewCommand toBatchReviewCommand(TagBatchReviewRequest request) {
        return new TagBatchReviewCommand(
                toTagIds(request == null ? null : request.getTagIds()),
                request == null ? null : request.getDecision(),
                TagCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()),
                request == null ? null : request.getReviewNote());
    }

    public static TagExtractionCommand toExtractionCommand(TagExtractionRequest request) {
        return new TagExtractionCommand(
                request == null ? null : request.getSourceContentType(),
                request == null ? null : request.getSourceContentId(),
                request == null ? null : request.getContentTitle(),
                request == null ? null : request.getContentText(),
                request == null ? null : request.getModelId(),
                request == null ? null : request.getModelName(),
                request == null ? null : request.getPromptVersionId(),
                request == null ? null : request.getMaxTags(),
                request == null ? null : request.getAllowNewTags(),
                request == null ? null : request.getRequestedBy());
    }

    public static TagCandidateApplyCommand toCandidateApplyCommand(TagCandidateApplyRequest request) {
        return new TagCandidateApplyCommand(
                request == null ? null : request.getAiCandidateId(),
                request == null || request.getSelectedTags() == null
                        ? null
                        : request.getSelectedTags().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toCandidateApplyItemCommand)
                                .toList(),
                request == null ? null : request.getReviewNote(),
                request == null ? null : request.getReviewedBy());
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

    public static TagAliasCreateCommand toAliasCreateCommand(TagAliasCreateRequest request) {
        return new TagAliasCreateCommand(
                TagAliasIdCodec.toDomain(request.getId()),
                TagIdCodec.toDomain(request.getTagId()),
                request.getName(),
                StringUtils.isBlank(request.getSource()) ? null : TagSource.from(request.getSource()));
    }

    public static TagAliasRemoveCommand toAliasRemoveCommand(TagAliasRemoveRequest request) {
        return new TagAliasRemoveCommand(TagAliasIdCodec.toDomain(request.getId()));
    }

    public static TagCategoryResponse toResponse(TagCategoryResult result) {
        return TagCategoryResponse.builder()
                .id(result == null ? null : result.getId())
                .name(result == null ? null : result.getName())
                .description(result == null ? null : result.getDescription())
                .status(result == null ? null : result.getStatus())
                .build();
    }

    public static TagResponse toResponse(TagResult result) {
        return TagResponse.builder()
                .id(result == null ? null : result.getId())
                .name(result == null ? null : result.getName())
                .categoryId(result == null ? null : result.getCategoryId())
                .categoryName(result == null ? null : result.getCategoryName())
                .description(result == null ? null : result.getDescription())
                .status(result == null ? null : result.getStatus())
                .source(result == null ? null : result.getSource())
                .reviewStatus(result == null ? null : result.getReviewStatus())
                .contentRefCount(result == null ? null : result.getContentRefCount())
                .createdAt(result == null ? null : result.getCreatedAt())
                .reviewedAt(result == null ? null : result.getReviewedAt())
                .build();
    }

    public static TagDetailResponse toResponse(TagDetailResult result) {
        return TagDetailResponse.builder()
                .tag(result == null || result.getTag() == null ? null : toResponse(result.getTag()))
                .aliases(
                        result == null || result.getAliases() == null
                                ? null
                                : result.getAliases().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .contentRefs(
                        result == null || result.getContentRefs() == null
                                ? null
                                : result.getContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    public static TagAliasResponse toResponse(TagAliasResult result) {
        return TagAliasResponse.builder()
                .id(result == null ? null : result.getId())
                .name(result == null ? null : result.getName())
                .source(result == null ? null : result.getSource())
                .build();
    }

    public static TagContentRefResponse toResponse(TagContentRefResult result) {
        return TagContentRefResponse.builder()
                .id(result == null ? null : result.getId())
                .contentType(result == null ? null : result.getContentType())
                .contentId(result == null ? null : result.getContentId())
                .contentTitle(result == null ? null : result.getContentTitle())
                .source(result == null ? null : result.getSource())
                .build();
    }

    public static TagMergePreviewResponse toResponse(TagMergePreviewResult result) {
        return TagMergePreviewResponse.builder()
                .sourceTag(result == null ? null : toResponse(result.getSourceTag()))
                .targetTag(result == null ? null : toResponse(result.getTargetTag()))
                .aliasesToMerge(
                        result == null || result.getAliasesToMerge() == null
                                ? null
                                : result.getAliasesToMerge().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .impactedContentRefs(
                        result == null || result.getImpactedContentRefs() == null
                                ? null
                                : result.getImpactedContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .pendingReviewCount(result == null ? null : result.getPendingReviewCount())
                .governedRecordCount(result == null ? null : result.getGovernedRecordCount())
                .build();
    }

    public static TagBatchMergePreviewResponse toResponse(TagBatchMergePreviewResult result) {
        return TagBatchMergePreviewResponse.builder()
                .sourceTags(
                        result == null || result.getSourceTags() == null
                                ? null
                                : result.getSourceTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .targetTag(result == null ? null : toResponse(result.getTargetTag()))
                .aliasesToMerge(
                        result == null || result.getAliasesToMerge() == null
                                ? null
                                : result.getAliasesToMerge().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .impactedContentRefs(
                        result == null || result.getImpactedContentRefs() == null
                                ? null
                                : result.getImpactedContentRefs().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .pendingReviewCount(result == null ? null : result.getPendingReviewCount())
                .governedRecordCount(result == null ? null : result.getGovernedRecordCount())
                .build();
    }

    public static TagGovernanceMetricsResponse toResponse(TagGovernanceMetricsResult result) {
        return TagGovernanceMetricsResponse.builder()
                .topTags(
                        result == null || result.getTopTags() == null
                                ? null
                                : result.getTopTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .categoryDistributions(
                        result == null || result.getCategoryDistributions() == null
                                ? null
                                : result.getCategoryDistributions().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .sourceRatios(
                        result == null || result.getSourceRatios() == null
                                ? null
                                : result.getSourceRatios().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .monthlyNewTags(
                        result == null || result.getMonthlyNewTags() == null
                                ? null
                                : result.getMonthlyNewTags().stream()
                                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                        .toList())
                .build();
    }

    public static TagExtractionResponse toResponse(TagExtractionResult result) {
        return TagExtractionResponse.builder()
                .aiCallId(result == null ? null : result.getAiCallId())
                .aiCandidateId(result == null ? null : result.getAiCandidateId())
                .status(result == null ? null : result.getStatus())
                .resultFormat(result == null ? null : result.getResultFormat())
                .resultPayload(result == null ? null : result.getResultPayload())
                .errorType(result == null ? null : result.getErrorType())
                .errorMessage(result == null ? null : result.getErrorMessage())
                .build();
    }

    public static TagGovernanceMetricsResponse.TagUsageMetric toResponse(
            TagGovernanceMetricsResult.TagUsageMetric result) {
        return TagGovernanceMetricsResponse.TagUsageMetric.builder()
                .tagName(result == null ? null : result.getTagName())
                .contentRefCount(result == null ? null : result.getContentRefCount())
                .build();
    }

    public static TagGovernanceMetricsResponse.CategoryDistributionMetric toResponse(
            TagGovernanceMetricsResult.CategoryDistributionMetric result) {
        return TagGovernanceMetricsResponse.CategoryDistributionMetric.builder()
                .categoryName(result == null ? null : result.getCategoryName())
                .tagCount(result == null ? null : result.getTagCount())
                .build();
    }

    public static TagGovernanceMetricsResponse.SourceRatioMetric toResponse(
            TagGovernanceMetricsResult.SourceRatioMetric result) {
        return TagGovernanceMetricsResponse.SourceRatioMetric.builder()
                .source(
                        result == null || result.getSource() == null
                                ? null
                                : result.getSource().value())
                .tagCount(result == null ? null : result.getTagCount())
                .build();
    }

    public static TagGovernanceMetricsResponse.MonthlyNewTagMetric toResponse(
            TagGovernanceMetricsResult.MonthlyNewTagMetric result) {
        return TagGovernanceMetricsResponse.MonthlyNewTagMetric.builder()
                .month(result == null ? null : result.getMonth())
                .tagCount(result == null ? null : result.getTagCount())
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
