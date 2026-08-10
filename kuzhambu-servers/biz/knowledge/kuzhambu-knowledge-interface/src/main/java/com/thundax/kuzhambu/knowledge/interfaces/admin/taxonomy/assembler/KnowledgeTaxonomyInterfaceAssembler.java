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
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryEntityHintQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.DiscoveryTagHintQuery;
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
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDetailRequest;
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

    public static DiscoveryTagHintQuery toTagHintQuery(String term) {
        return new DiscoveryTagHintQuery(term);
    }

    public static DiscoveryEntityHintQuery toEntityHintQuery(String term) {
        return new DiscoveryEntityHintQuery(term);
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

    public static TagDetailRequest toTagIdRequest(TagDetailRequest request) {
        return request;
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
        TagCategoryResponse response = new TagCategoryResponse();
        response.setId(result == null ? null : result.getId());
        response.setName(result == null ? null : result.getName());
        response.setDescription(result == null ? null : result.getDescription());
        response.setStatus(result == null ? null : result.getStatus());
        return response;
    }

    public static TagResponse toResponse(TagResult result) {
        TagResponse response = new TagResponse();
        response.setId(result == null ? null : result.getId());
        response.setName(result == null ? null : result.getName());
        response.setCategoryId(result == null ? null : result.getCategoryId());
        response.setCategoryName(result == null ? null : result.getCategoryName());
        response.setDescription(result == null ? null : result.getDescription());
        response.setStatus(result == null ? null : result.getStatus());
        response.setSource(result == null ? null : result.getSource());
        response.setReviewStatus(result == null ? null : result.getReviewStatus());
        response.setContentRefCount(result == null ? null : result.getContentRefCount());
        response.setCreatedAt(result == null ? null : result.getCreatedAt());
        response.setReviewedAt(result == null ? null : result.getReviewedAt());
        return response;
    }

    public static TagDetailResponse toResponse(TagDetailResult result) {
        TagDetailResponse response = new TagDetailResponse();
        response.setTag(result == null || result.getTag() == null ? null : toResponse(result.getTag()));
        response.setAliases(
                result == null || result.getAliases() == null
                        ? null
                        : result.getAliases().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setContentRefs(
                result == null || result.getContentRefs() == null
                        ? null
                        : result.getContentRefs().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        return response;
    }

    public static TagAliasResponse toResponse(TagAliasResult result) {
        TagAliasResponse response = new TagAliasResponse();
        response.setId(result == null ? null : result.getId());
        response.setName(result == null ? null : result.getName());
        response.setSource(result == null ? null : result.getSource());
        return response;
    }

    public static TagContentRefResponse toResponse(TagContentRefResult result) {
        TagContentRefResponse response = new TagContentRefResponse();
        response.setId(result == null ? null : result.getId());
        response.setContentType(result == null ? null : result.getContentType());
        response.setContentId(result == null ? null : result.getContentId());
        response.setContentTitle(result == null ? null : result.getContentTitle());
        response.setSource(result == null ? null : result.getSource());
        return response;
    }

    public static TagMergePreviewResponse toResponse(TagMergePreviewResult result) {
        TagMergePreviewResponse response = new TagMergePreviewResponse();
        response.setSourceTag(result == null ? null : toResponse(result.getSourceTag()));
        response.setTargetTag(result == null ? null : toResponse(result.getTargetTag()));
        response.setAliasesToMerge(
                result == null || result.getAliasesToMerge() == null
                        ? null
                        : result.getAliasesToMerge().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setImpactedContentRefs(
                result == null || result.getImpactedContentRefs() == null
                        ? null
                        : result.getImpactedContentRefs().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setPendingReviewCount(result == null ? null : result.getPendingReviewCount());
        response.setGovernedRecordCount(result == null ? null : result.getGovernedRecordCount());
        return response;
    }

    public static TagBatchMergePreviewResponse toResponse(TagBatchMergePreviewResult result) {
        TagBatchMergePreviewResponse response = new TagBatchMergePreviewResponse();
        response.setSourceTags(
                result == null || result.getSourceTags() == null
                        ? null
                        : result.getSourceTags().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setTargetTag(result == null ? null : toResponse(result.getTargetTag()));
        response.setAliasesToMerge(
                result == null || result.getAliasesToMerge() == null
                        ? null
                        : result.getAliasesToMerge().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setImpactedContentRefs(
                result == null || result.getImpactedContentRefs() == null
                        ? null
                        : result.getImpactedContentRefs().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setPendingReviewCount(result == null ? null : result.getPendingReviewCount());
        response.setGovernedRecordCount(result == null ? null : result.getGovernedRecordCount());
        return response;
    }

    public static TagGovernanceMetricsResponse toResponse(TagGovernanceMetricsResult result) {
        TagGovernanceMetricsResponse response = new TagGovernanceMetricsResponse();
        response.setTopTags(
                result == null || result.getTopTags() == null
                        ? null
                        : result.getTopTags().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setCategoryDistributions(
                result == null || result.getCategoryDistributions() == null
                        ? null
                        : result.getCategoryDistributions().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setSourceRatios(
                result == null || result.getSourceRatios() == null
                        ? null
                        : result.getSourceRatios().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        response.setMonthlyNewTags(
                result == null || result.getMonthlyNewTags() == null
                        ? null
                        : result.getMonthlyNewTags().stream()
                                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                                .toList());
        return response;
    }

    public static TagExtractionResponse toResponse(TagExtractionResult result) {
        TagExtractionResponse response = new TagExtractionResponse();
        response.setAiCallId(result == null ? null : result.getAiCallId());
        response.setAiCandidateId(result == null ? null : result.getAiCandidateId());
        response.setStatus(result == null ? null : result.getStatus());
        response.setResultFormat(result == null ? null : result.getResultFormat());
        response.setResultPayload(result == null ? null : result.getResultPayload());
        response.setErrorType(result == null ? null : result.getErrorType());
        response.setErrorMessage(result == null ? null : result.getErrorMessage());
        return response;
    }

    public static TagGovernanceMetricsResponse.TagUsageMetric toResponse(
            TagGovernanceMetricsResult.TagUsageMetric result) {
        TagGovernanceMetricsResponse.TagUsageMetric response = new TagGovernanceMetricsResponse.TagUsageMetric();
        response.setTagName(result == null ? null : result.getTagName());
        response.setContentRefCount(result == null ? null : result.getContentRefCount());
        return response;
    }

    public static TagGovernanceMetricsResponse.CategoryDistributionMetric toResponse(
            TagGovernanceMetricsResult.CategoryDistributionMetric result) {
        TagGovernanceMetricsResponse.CategoryDistributionMetric response =
                new TagGovernanceMetricsResponse.CategoryDistributionMetric();
        response.setCategoryName(result == null ? null : result.getCategoryName());
        response.setTagCount(result == null ? null : result.getTagCount());
        return response;
    }

    public static TagGovernanceMetricsResponse.SourceRatioMetric toResponse(
            TagGovernanceMetricsResult.SourceRatioMetric result) {
        TagGovernanceMetricsResponse.SourceRatioMetric response = new TagGovernanceMetricsResponse.SourceRatioMetric();
        response.setSource(
                result == null || result.getSource() == null
                        ? null
                        : result.getSource().value());
        response.setTagCount(result == null ? null : result.getTagCount());
        return response;
    }

    public static TagGovernanceMetricsResponse.MonthlyNewTagMetric toResponse(
            TagGovernanceMetricsResult.MonthlyNewTagMetric result) {
        TagGovernanceMetricsResponse.MonthlyNewTagMetric response =
                new TagGovernanceMetricsResponse.MonthlyNewTagMetric();
        response.setMonth(result == null ? null : result.getMonth());
        response.setTagCount(result == null ? null : result.getTagCount());
        return response;
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
