package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller;

import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.refinement.service.KnowledgeGraphRefinementApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.assembler.KnowledgeGraphRefinementInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.request.RefinementRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response.RefinementResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-图谱精修", description = "知识图谱精修工作台")
@SysLogger(module = {"知识", "图谱精修"})
@RequestMapping("/api/knowledge/refinement")
@WrappedApiController
public class KnowledgeGraphRefinementController {

    private final KnowledgeGraphRefinementApplicationService refinementService;

    public KnowledgeGraphRefinementController(KnowledgeGraphRefinementApplicationService refinementService) {
        this.refinementService = refinementService;
    }

    @Operation(summary = "分页查询精修任务", description = "knowledge:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class)
    })
    @HasPermission("knowledge:refinement:view")
    @SysLogger("精修任务分页")
    @PostMapping("task/page")
    public PageResponse<RefinementResponses.WorkbenchItemResponse> pageTasks(
            @Valid @RequestBody RefinementRequests.TaskPageRequest request) {
        return PageResponseHelper.fromPageResult(
                refinementService.pageTasks(KnowledgeGraphRefinementInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphRefinementInterfaceAssembler::toResponse);
    }

    @Operation(summary = "打开精修任务", description = "knowledge:refinement:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class)
    })
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("打开精修任务")
    @PostMapping("task/open")
    public RefinementResponses.DetailResponse openTask(@Valid @RequestBody RefinementRequests.TaskOpenRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.openTask(
                request == null ? null : request.getGraphVersionId(), request == null ? null : request.getOpenedBy()));
    }

    @Operation(summary = "获取精修任务详情", description = "knowledge:refinement:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class)
    })
    @HasPermission("knowledge:refinement:view")
    @SysLogger("精修任务详情")
    @PostMapping("task/detail")
    public RefinementResponses.DetailResponse getTaskDetail(
            @Valid @RequestBody RefinementRequests.TaskDetailRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(
                refinementService.getTaskDetail(KnowledgeGraphRefinementInterfaceAssembler.toDetailQuery(request)));
    }

    @Operation(summary = "新增实体草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增实体草稿")
    @PostMapping("entity/add")
    public RefinementResponses.EntityResponse addEntity(
            @Valid @RequestBody RefinementRequests.EntityUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(
                refinementService.upsertEntity(KnowledgeGraphRefinementInterfaceAssembler.toEntityCommand(request)));
    }

    @Operation(summary = "更新实体草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("更新实体草稿")
    @PostMapping("entity/update")
    public RefinementResponses.EntityResponse updateEntity(
            @Valid @RequestBody RefinementRequests.EntityUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(
                refinementService.upsertEntity(KnowledgeGraphRefinementInterfaceAssembler.toEntityCommand(request)));
    }

    @Operation(summary = "确认实体草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("确认实体草稿")
    @PostMapping("entity/confirm")
    public RefinementResponses.EntityResponse confirmEntity(
            @Valid @RequestBody RefinementRequests.EntityConfirmRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.confirmEntity(
                KnowledgeGraphRefinementInterfaceAssembler.toConfirmEntityCommand(request)));
    }

    @Operation(summary = "删除实体草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("删除实体草稿")
    @PostMapping("entity/delete")
    public void deleteEntity(@Valid @RequestBody RefinementRequests.EntityDeleteRequest request) {
        refinementService.deleteEntity(KnowledgeGraphRefinementInterfaceAssembler.toDeleteEntityCommand(request));
    }

    @Operation(summary = "新增关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增关系草稿")
    @PostMapping("relation/add")
    public RefinementResponses.RelationResponse addRelation(
            @Valid @RequestBody RefinementRequests.RelationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toRelationCommand(request)));
    }

    @Operation(summary = "更新关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("更新关系草稿")
    @PostMapping("relation/update")
    public RefinementResponses.RelationResponse updateRelation(
            @Valid @RequestBody RefinementRequests.RelationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toRelationCommand(request)));
    }

    @Operation(summary = "确认关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("确认关系草稿")
    @PostMapping("relation/confirm")
    public RefinementResponses.RelationResponse confirmRelation(
            @Valid @RequestBody RefinementRequests.RelationConfirmRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.confirmRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toConfirmRelationCommand(request)));
    }

    @Operation(summary = "删除关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("删除关系草稿")
    @PostMapping("relation/delete")
    public void deleteRelation(@Valid @RequestBody RefinementRequests.RelationDeleteRequest request) {
        refinementService.deleteRelation(KnowledgeGraphRefinementInterfaceAssembler.toDeleteRelationCommand(request));
    }

    @Operation(summary = "应用精修任务", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("应用精修任务")
    @PostMapping("task/apply")
    public RefinementResponses.DetailResponse applyTask(
            @Valid @RequestBody RefinementRequests.TaskApplyRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.applyTask(
                request == null ? null : request.getRefinementTaskId(),
                request == null ? null : request.getAppliedBy()));
    }

    @Operation(summary = "获取质量汇总", description = "knowledge:refinement:view")
    @HasPermission("knowledge:refinement:view")
    @SysLogger("质量汇总")
    @PostMapping("quality/summary")
    public RefinementResponses.QualitySummaryResponse qualitySummary(
            @Valid @RequestBody RefinementRequests.QualitySummaryRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(
                refinementService.qualitySummary(request == null ? null : request.getRefinementTaskId()));
    }

    @Operation(summary = "新增世系节点草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增世系节点草稿")
    @PostMapping("lineage-node/add")
    public RefinementResponses.LineageNodeResponse addLineageNode(
            @Valid @RequestBody RefinementRequests.LineageNodeUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertLineageNode(
                KnowledgeGraphRefinementInterfaceAssembler.toLineageNodeCommand(request)));
    }

    @Operation(summary = "更新世系节点草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("更新世系节点草稿")
    @PostMapping("lineage-node/update")
    public RefinementResponses.LineageNodeResponse updateLineageNode(
            @Valid @RequestBody RefinementRequests.LineageNodeUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertLineageNode(
                KnowledgeGraphRefinementInterfaceAssembler.toLineageNodeCommand(request)));
    }

    @Operation(summary = "确认世系节点草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("确认世系节点草稿")
    @PostMapping("lineage-node/confirm")
    public RefinementResponses.LineageNodeResponse confirmLineageNode(
            @Valid @RequestBody RefinementRequests.LineageNodeConfirmRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.confirmLineageNode(
                KnowledgeGraphRefinementInterfaceAssembler.toConfirmLineageNodeCommand(request)));
    }

    @Operation(summary = "删除世系节点草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("删除世系节点草稿")
    @PostMapping("lineage-node/delete")
    public void deleteLineageNode(@Valid @RequestBody RefinementRequests.LineageNodeDeleteRequest request) {
        refinementService.deleteLineageNode(
                KnowledgeGraphRefinementInterfaceAssembler.toDeleteLineageNodeCommand(request));
    }

    @Operation(summary = "新增世系关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增世系关系草稿")
    @PostMapping("lineage-relation/add")
    public RefinementResponses.LineageRelationResponse addLineageRelation(
            @Valid @RequestBody RefinementRequests.LineageRelationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertLineageRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toLineageRelationCommand(request)));
    }

    @Operation(summary = "更新世系关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("更新世系关系草稿")
    @PostMapping("lineage-relation/update")
    public RefinementResponses.LineageRelationResponse updateLineageRelation(
            @Valid @RequestBody RefinementRequests.LineageRelationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertLineageRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toLineageRelationCommand(request)));
    }

    @Operation(summary = "确认世系关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("确认世系关系草稿")
    @PostMapping("lineage-relation/confirm")
    public RefinementResponses.LineageRelationResponse confirmLineageRelation(
            @Valid @RequestBody RefinementRequests.LineageRelationConfirmRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.confirmLineageRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toConfirmLineageRelationCommand(request)));
    }

    @Operation(summary = "删除世系关系草稿", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("删除世系关系草稿")
    @PostMapping("lineage-relation/delete")
    public void deleteLineageRelation(@Valid @RequestBody RefinementRequests.LineageRelationDeleteRequest request) {
        refinementService.deleteLineageRelation(
                KnowledgeGraphRefinementInterfaceAssembler.toDeleteLineageRelationCommand(request));
    }

    @Operation(summary = "新增或更新质量标注", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增或更新质量标注")
    @PostMapping("annotation/update")
    public RefinementResponses.AnnotationResponse upsertAnnotation(
            @Valid @RequestBody RefinementRequests.AnnotationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertAnnotation(
                KnowledgeGraphRefinementInterfaceAssembler.toAnnotationCommand(request)));
    }

    @Operation(summary = "新增质量标注", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("新增质量标注")
    @PostMapping("annotation/add")
    public RefinementResponses.AnnotationResponse addAnnotation(
            @Valid @RequestBody RefinementRequests.AnnotationUpsertRequest request) {
        return KnowledgeGraphRefinementInterfaceAssembler.toResponse(refinementService.upsertAnnotation(
                KnowledgeGraphRefinementInterfaceAssembler.toAnnotationCommand(request)));
    }

    @Operation(summary = "删除质量标注", description = "knowledge:refinement:edit")
    @HasPermission("knowledge:refinement:edit")
    @SysLogger("删除质量标注")
    @PostMapping("annotation/delete")
    public void deleteAnnotation(@Valid @RequestBody RefinementRequests.AnnotationDeleteRequest request) {
        refinementService.deleteAnnotation(
                KnowledgeGraphRefinementInterfaceAssembler.toDeleteAnnotationCommand(request));
    }

    @Operation(summary = "分页获取质量标注", description = "knowledge:refinement:view")
    @HasPermission("knowledge:refinement:view")
    @SysLogger("分页获取质量标注")
    @PostMapping("annotation/page")
    public PageResponse<RefinementResponses.AnnotationResponse> pageAnnotations(
            @Valid @RequestBody RefinementRequests.AnnotationPageRequest request) {
        return PageResponseHelper.fromPageResult(
                refinementService.pageAnnotations(
                        KnowledgeGraphRefinementInterfaceAssembler.toAnnotationPageQuery(request)),
                KnowledgeGraphRefinementInterfaceAssembler::toResponse);
    }
}
