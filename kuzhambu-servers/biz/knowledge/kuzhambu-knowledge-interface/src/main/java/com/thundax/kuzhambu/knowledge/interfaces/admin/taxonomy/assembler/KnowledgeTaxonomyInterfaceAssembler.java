package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.assembler;

import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.SynonymUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagAliasRemoveCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCategoryUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagCreateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagDeprecateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagMergeCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagReviewCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagStatusCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.command.TagUpdateCommand;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.SynonymPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagCategoryPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagGovernanceMetricsQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagMergePreviewQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.query.TagReviewPageQuery;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.SynonymResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagAliasResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagCategoryResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagContentRefResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagDetailResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagGovernanceMetricsResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagMergePreviewResult;
import com.thundax.kuzhambu.knowledge.application.taxonomy.result.TagResult;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.SynonymIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagAliasIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagCategoryIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.SynonymStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagCategoryStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagReviewStatus;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagStatus;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.SynonymCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.SynonymPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.SynonymRemoveRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.SynonymStatusRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.SynonymUpdateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasRemoveRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryStatusRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCategoryUpdateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagDetailRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagGovernanceMetricsRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagMergeRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagReviewPageRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagReviewRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagStatusRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagUpdateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.SynonymResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagAliasResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagCategoryResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagContentRefResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagDetailResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagGovernanceMetricsResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagMergePreviewResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagResponse;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public final class KnowledgeTaxonomyInterfaceAssembler {

    private KnowledgeTaxonomyInterfaceAssembler() {}

    public static TagCategoryPageQuery toQuery(TagCategoryPageRequest request) {
        TagCategoryPageQuery query = new TagCategoryPageQuery();
        query.setName(request == null ? null : request.getName());
        query.setStatus(
                request == null || StringUtils.isBlank(request.getStatus())
                        ? null
                        : TagCategoryStatus.from(request.getStatus()));
        query.setSortDirection(resolveSortDirection(request == null ? null : request.getSortDirection()));
        return query;
    }

    public static TagCategoryCreateCommand toCategoryCreateCommand(TagCategoryCreateRequest request) {
        return new TagCategoryCreateCommand(
                TagCategoryIdCodec.toDomain(request.getId()),
                request.getName(),
                request.getDescription(),
                request.getPriority(),
                request.getStatus() == null ? TagCategoryStatus.ENABLED : TagCategoryStatus.from(request.getStatus()));
    }

    public static TagCategoryUpdateCommand toCategoryUpdateCommand(TagCategoryUpdateRequest request) {
        return new TagCategoryUpdateCommand(
                TagCategoryIdCodec.toDomain(request.getId()),
                request.getName(),
                request.getDescription(),
                request.getPriority());
    }

    public static TagCategoryStatusCommand toCategoryStatusCommand(TagCategoryStatusRequest request) {
        return new TagCategoryStatusCommand(
                TagCategoryIdCodec.toDomain(request.getId()), TagCategoryStatus.from(request.getStatus()));
    }

    public static TagPageQuery toQuery(TagPageRequest request) {
        TagPageQuery query = new TagPageQuery();
        query.setName(request == null ? null : request.getName());
        query.setCategoryId(TagCategoryIdCodec.toDomain(request == null ? null : request.getCategoryId()));
        query.setStatus(
                request == null || StringUtils.isBlank(request.getStatus())
                        ? null
                        : TagStatus.from(request.getStatus()));
        query.setSource(
                request == null || StringUtils.isBlank(request.getSource())
                        ? null
                        : TagSource.from(request.getSource()));
        query.setReviewStatus(
                request == null || StringUtils.isBlank(request.getReviewStatus())
                        ? null
                        : TagReviewStatus.from(request.getReviewStatus()));
        query.setSortDirection(resolveSortDirection(request == null ? null : request.getSortDirection()));
        return query;
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

    public static TagDeprecateCommand toDeprecateCommand(TagDeprecateRequest request) {
        return new TagDeprecateCommand(TagIdCodec.toDomain(request.getId()));
    }

    public static TagGovernanceMetricsQuery toMetricsQuery(TagGovernanceMetricsRequest request) {
        return new TagGovernanceMetricsQuery(
                request == null ? null : request.getTopLimit(), request == null ? null : request.getRecentMonths());
    }

    public static TagDetailRequest toTagIdRequest(TagDetailRequest request) {
        return request;
    }

    public static TagReviewPageQuery toQuery(TagReviewPageRequest request) {
        TagReviewPageQuery query = new TagReviewPageQuery();
        query.setName(request == null ? null : request.getName());
        query.setSource(
                request == null || StringUtils.isBlank(request.getSource())
                        ? null
                        : TagSource.from(request.getSource()));
        query.setSortDirection(resolveSortDirection(request == null ? null : request.getSortDirection()));
        return query;
    }

    public static TagReviewCommand toReviewCommand(TagReviewRequest request) {
        return new TagReviewCommand(
                TagIdCodec.toDomain(request.getId()), request.getDecision(), request.getReviewNote());
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

    public static SynonymPageQuery toQuery(SynonymPageRequest request) {
        SynonymPageQuery query = new SynonymPageQuery();
        query.setTerm(request == null ? null : request.getTerm());
        query.setSynonym(request == null ? null : request.getSynonym());
        query.setStatus(
                request == null || StringUtils.isBlank(request.getStatus())
                        ? null
                        : SynonymStatus.from(request.getStatus()));
        query.setSortDirection(resolveSortDirection(request == null ? null : request.getSortDirection()));
        return query;
    }

    public static SynonymCreateCommand toCreateCommand(SynonymCreateRequest request) {
        return new SynonymCreateCommand(
                SynonymIdCodec.toDomain(request.getId()),
                request.getTerm(),
                request.getSynonym(),
                StringUtils.isBlank(request.getStatus()) ? null : SynonymStatus.from(request.getStatus()));
    }

    public static SynonymUpdateCommand toUpdateCommand(SynonymUpdateRequest request) {
        return new SynonymUpdateCommand(
                SynonymIdCodec.toDomain(request.getId()), request.getTerm(), request.getSynonym());
    }

    public static SynonymStatusCommand toStatusCommand(SynonymStatusRequest request) {
        return new SynonymStatusCommand(
                SynonymIdCodec.toDomain(request.getId()), SynonymStatus.from(request.getStatus()));
    }

    public static SynonymRemoveCommand toRemoveCommand(SynonymRemoveRequest request) {
        return new SynonymRemoveCommand(SynonymIdCodec.toDomain(request.getId()));
    }

    public static TagCategoryResponse toResponse(TagCategoryResult result) {
        TagCategoryResponse response = new TagCategoryResponse();
        response.setId(result == null ? null : result.getId());
        response.setName(result == null ? null : result.getName());
        response.setDescription(result == null ? null : result.getDescription());
        response.setPriority(result == null ? null : result.getPriority());
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

    public static SynonymResponse toResponse(SynonymResult result) {
        SynonymResponse response = new SynonymResponse();
        response.setId(result == null ? null : result.getId());
        response.setTerm(result == null ? null : result.getTerm());
        response.setSynonym(result == null ? null : result.getSynonym());
        response.setStatus(result == null ? null : result.getStatus());
        return response;
    }

    public static List<SynonymResponse> toResponses(List<SynonymResult> list) {
        return list == null
                ? List.of()
                : list.stream()
                        .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                        .toList();
    }

    private static SortDirection resolveSortDirection(String sortDirection) {
        return StringUtils.isBlank(sortDirection)
                ? SortDirection.ASC
                : SortDirection.valueOf(sortDirection.trim().toUpperCase());
    }
}
