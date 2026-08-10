package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.taxonomy.service.TaxonomyApplicationService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.codec.TagIdCodec;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.assembler.KnowledgeTaxonomyInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasCreateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagAliasRemoveRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchDeprecateRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchMergeRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagBatchReviewRequest;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request.TagCandidateApplyRequest;
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
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagDetailResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagExtractionResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagGovernanceMetricsResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagMergePreviewResponse;
import com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response.TagResponse;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-标签治理", description = "知识标签治理")
@SysLogger(module = {"知识", "标签治理"})
@RequestMapping("/api/knowledge/taxonomy")
@WrappedApiController
public class KnowledgeTaxonomyController {

    private final TaxonomyApplicationService taxonomyService;

    public KnowledgeTaxonomyController(TaxonomyApplicationService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @Operation(summary = "分页查询标签分类", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "分类分页")
    @PostMapping("category/page")
    public PageResponse<TagCategoryResponse> pageCategories(@Valid @RequestBody TagCategoryPageRequest request) {
        return PageResponseHelper.fromPageResult(
                taxonomyService.pageCategories(
                        KnowledgeTaxonomyInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeTaxonomyInterfaceAssembler::toResponse);
    }

    @Operation(summary = "新增标签分类", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "创建分类")
    @PostMapping("category/create")
    public Boolean createCategory(@Valid @RequestBody TagCategoryCreateRequest request) {
        taxonomyService.createCategory(KnowledgeTaxonomyInterfaceAssembler.toCategoryCreateCommand(request));
        return true;
    }

    @Operation(summary = "更新标签分类", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "更新分类")
    @PostMapping("category/update")
    public Boolean updateCategory(@Valid @RequestBody TagCategoryUpdateRequest request) {
        taxonomyService.updateCategory(KnowledgeTaxonomyInterfaceAssembler.toCategoryUpdateCommand(request));
        return true;
    }

    @Operation(summary = "变更标签分类状态", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "变更分类状态")
    @PostMapping("category/status/change")
    public Boolean changeCategoryStatus(@Valid @RequestBody TagCategoryStatusRequest request) {
        taxonomyService.changeCategoryStatus(KnowledgeTaxonomyInterfaceAssembler.toCategoryStatusCommand(request));
        return true;
    }

    @Operation(summary = "分页查询标签", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "标签分页")
    @PostMapping("tag/page")
    public PageResponse<TagResponse> pageTags(@Valid @RequestBody TagPageRequest request) {
        return PageResponseHelper.fromPageResult(
                taxonomyService.pageTags(
                        KnowledgeTaxonomyInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeTaxonomyInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询标签详情", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "标签详情")
    @PostMapping("tag/detail/get")
    public TagDetailResponse getTagDetail(@Valid @RequestBody TagDetailRequest request) {
        TagId tagId = TagIdCodec.toDomain(request.getTagId());
        return KnowledgeTaxonomyInterfaceAssembler.toResponse(taxonomyService.getTagDetail(tagId));
    }

    @Operation(summary = "创建标签", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "创建标签")
    @PostMapping("tag/create")
    public Boolean createTag(@Valid @RequestBody TagCreateRequest request) {
        taxonomyService.createTag(KnowledgeTaxonomyInterfaceAssembler.toCreateCommand(request));
        return true;
    }

    @Operation(summary = "更新标签", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "更新标签")
    @PostMapping("tag/update")
    public Boolean updateTag(@Valid @RequestBody TagUpdateRequest request) {
        taxonomyService.updateTag(KnowledgeTaxonomyInterfaceAssembler.toUpdateCommand(request));
        return true;
    }

    @Operation(summary = "变更标签状态", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "变更标签状态")
    @PostMapping("tag/status/change")
    public Boolean changeTagStatus(@Valid @RequestBody TagStatusRequest request) {
        taxonomyService.changeTagStatus(KnowledgeTaxonomyInterfaceAssembler.toStatusCommand(request));
        return true;
    }

    @Operation(summary = "分页查询待审核标签", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "待审核标签分页")
    @PostMapping("tag/review/page")
    public PageResponse<TagResponse> pagePendingTags(@Valid @RequestBody TagReviewPageRequest request) {
        return PageResponseHelper.fromPageResult(
                taxonomyService.pagePendingTags(
                        KnowledgeTaxonomyInterfaceAssembler.toQuery(request),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeTaxonomyInterfaceAssembler::toResponse);
    }

    @Operation(summary = "审核标签", description = "knowledge:taxonomy:review")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:review")
    @SysLogger(value = "审核标签")
    @PostMapping("tag/review")
    public Boolean reviewTag(@Valid @RequestBody TagReviewRequest request) {
        taxonomyService.reviewTag(KnowledgeTaxonomyInterfaceAssembler.toReviewCommand(request));
        return true;
    }

    @Operation(summary = "批量审核标签", description = "knowledge:taxonomy:review")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:review")
    @SysLogger(value = "批量审核标签")
    @PostMapping("tag/review/list")
    public Boolean reviewTagBatch(@Valid @RequestBody TagBatchReviewRequest request) {
        taxonomyService.batchReviewTags(KnowledgeTaxonomyInterfaceAssembler.toBatchReviewCommand(request));
        return true;
    }

    @Operation(summary = "AI 抽取标签", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "AI抽取标签")
    @PostMapping("tag/extract")
    public TagExtractionResponse extractTags(@Valid @RequestBody TagExtractionRequest request) {
        return KnowledgeTaxonomyInterfaceAssembler.toResponse(
                taxonomyService.extractTags(KnowledgeTaxonomyInterfaceAssembler.toExtractionCommand(request)));
    }

    @Operation(summary = "应用 AI 标签候选", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "应用AI标签候选")
    @PostMapping("tag/extract/apply")
    public Boolean applyExtractedTags(@Valid @RequestBody TagCandidateApplyRequest request) {
        taxonomyService.applyExtractedTags(KnowledgeTaxonomyInterfaceAssembler.toCandidateApplyCommand(request));
        return true;
    }

    @Operation(summary = "预览标签合并影响", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "预览标签合并")
    @PostMapping("tag/merge/preview")
    public TagMergePreviewResponse previewTagMergeImpact(@Valid @RequestBody TagMergeRequest request) {
        return KnowledgeTaxonomyInterfaceAssembler.toResponse(taxonomyService.previewTagMergeImpact(
                KnowledgeTaxonomyInterfaceAssembler.toMergePreviewQuery(request)));
    }

    @Operation(summary = "批量预览标签合并影响", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "批量预览标签合并")
    @PostMapping("tag/merge/list/preview")
    public TagBatchMergePreviewResponse previewTagBatchMergeImpact(@Valid @RequestBody TagBatchMergeRequest request) {
        return KnowledgeTaxonomyInterfaceAssembler.toResponse(taxonomyService.previewTagBatchMergeImpact(
                KnowledgeTaxonomyInterfaceAssembler.toBatchMergePreviewQuery(request)));
    }

    @Operation(summary = "执行标签合并", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "执行标签合并")
    @PostMapping("tag/merge/apply")
    public Boolean applyTagMerge(@Valid @RequestBody TagMergeRequest request) {
        taxonomyService.applyTagMerge(KnowledgeTaxonomyInterfaceAssembler.toMergeCommand(request));
        return true;
    }

    @Operation(summary = "批量执行标签合并", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "批量执行标签合并")
    @PostMapping("tag/merge/list/apply")
    public Boolean applyTagBatchMerge(@Valid @RequestBody TagBatchMergeRequest request) {
        taxonomyService.applyTagBatchMerge(KnowledgeTaxonomyInterfaceAssembler.toBatchMergeCommand(request));
        return true;
    }

    @Operation(summary = "废弃标签", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "废弃标签")
    @PostMapping("tag/deprecate")
    public Boolean deprecateTag(@Valid @RequestBody TagDeprecateRequest request) {
        taxonomyService.deprecateTag(KnowledgeTaxonomyInterfaceAssembler.toDeprecateCommand(request));
        return true;
    }

    @Operation(summary = "批量废弃标签", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "批量废弃标签")
    @PostMapping("tag/deprecate/list")
    public Boolean deprecateTags(@Valid @RequestBody TagBatchDeprecateRequest request) {
        taxonomyService.batchDeprecateTags(KnowledgeTaxonomyInterfaceAssembler.toBatchDeprecateCommand(request));
        return true;
    }

    @Operation(summary = "查询标签治理统计", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "标签治理统计")
    @PostMapping("tag/metrics/list")
    public TagGovernanceMetricsResponse getTagGovernanceMetrics(
            @Valid @RequestBody TagGovernanceMetricsRequest request) {
        return KnowledgeTaxonomyInterfaceAssembler.toResponse(
                taxonomyService.getTagGovernanceMetrics(KnowledgeTaxonomyInterfaceAssembler.toMetricsQuery(request)));
    }

    @Operation(summary = "查询标签别名", description = "knowledge:taxonomy:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:view")
    @SysLogger(value = "标签别名列表")
    @PostMapping("tag/alias/list")
    public List<TagAliasResponse> listTagAliases(@Valid @RequestBody TagDetailRequest request) {
        TagId tagId = TagIdCodec.toDomain(request.getTagId());
        return taxonomyService.listTagAliases(tagId).stream()
                .map(KnowledgeTaxonomyInterfaceAssembler::toResponse)
                .toList();
    }

    @Operation(summary = "新增标签别名", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "创建标签别名")
    @PostMapping("tag/alias/create")
    public Boolean createTagAlias(@Valid @RequestBody TagAliasCreateRequest request) {
        taxonomyService.createTagAlias(KnowledgeTaxonomyInterfaceAssembler.toAliasCreateCommand(request));
        return true;
    }

    @Operation(summary = "删除标签别名", description = "knowledge:taxonomy:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:taxonomy:edit")
    @SysLogger(value = "删除标签别名")
    @PostMapping("tag/alias/remove")
    public Boolean removeTagAlias(@Valid @RequestBody TagAliasRemoveRequest request) {
        taxonomyService.removeTagAlias(KnowledgeTaxonomyInterfaceAssembler.toAliasRemoveCommand(request));
        return true;
    }
}
