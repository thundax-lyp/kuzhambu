package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.graph.service.KnowledgeGraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.KnowledgeGraphExtractionInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-图谱抽取", description = "知识图谱抽取任务")
@SysLogger(module = {"知识", "图谱抽取"})
@RequestMapping("/api/knowledge/graph-extraction")
@WrappedApiController
public class KnowledgeGraphExtractionController {

    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";

    private final KnowledgeGraphExtractionApplicationService extractionService;

    public KnowledgeGraphExtractionController(KnowledgeGraphExtractionApplicationService extractionService) {
        this.extractionService = extractionService;
    }

    @Operation(summary = "创建抽取任务", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "创建抽取任务")
    @PostMapping("task/add")
    public GraphExtractionResponses.TaskResponse addTask(
            @Valid @RequestBody GraphExtractionRequests.CreateRequest request) {
        String taskType = request == null ? null : request.getTaskType();
        return switch (taskType) {
            case TASK_TYPE_RELATION ->
                KnowledgeGraphExtractionInterfaceAssembler.toResponse(extractionService.requestRelationExtraction(
                        KnowledgeGraphExtractionInterfaceAssembler.toRelationCommand(request)));
            case TASK_TYPE_GRAPH ->
                KnowledgeGraphExtractionInterfaceAssembler.toResponse(extractionService.requestGraphExtraction(
                        KnowledgeGraphExtractionInterfaceAssembler.toGraphCommand(request)));
            case TASK_TYPE_LINEAGE ->
                KnowledgeGraphExtractionInterfaceAssembler.toResponse(extractionService.requestLineageExtraction(
                        KnowledgeGraphExtractionInterfaceAssembler.toLineageCommand(request)));
            default -> throw new BizException("Unsupported knowledge graph extraction task type: " + taskType);
        };
    }

    @Operation(summary = "分页查询抽取任务", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "抽取任务分页")
    @PostMapping("task/page")
    public PageResponse<GraphExtractionResponses.TaskResponse> pageTasks(
            @Valid @RequestBody GraphExtractionRequests.PageTaskRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageTasks(
                        request == null ? null : request.getTaskType(),
                        request == null ? null : request.getStatus(),
                        request == null ? null : request.getSourceContentType(),
                        request == null ? null : request.getSourceContentId(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询抽取任务详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "抽取任务详情")
    @PostMapping("task/get")
    public GraphExtractionResponses.TaskResponse getTaskDetail(
            @Valid @RequestBody GraphExtractionRequests.TaskIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getTaskDetail(KnowledgeGraphExtractionInterfaceAssembler.toTaskId(request)));
    }

    @Operation(summary = "应用抽取候选结果", description = "knowledge:graph:apply")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:apply")
    @SysLogger(value = "应用抽取候选结果")
    @PostMapping("task/apply")
    public GraphExtractionResponses.TaskResponse applyTaskCandidate(
            @Valid @RequestBody GraphExtractionRequests.TaskIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.applyTaskCandidate(KnowledgeGraphExtractionInterfaceAssembler.toTaskId(request)));
    }

    @Operation(summary = "分页查询图谱版本", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱版本分页")
    @PostMapping("version/page")
    public PageResponse<GraphExtractionResponses.VersionResponse> pageVersions(
            @Valid @RequestBody GraphExtractionRequests.VersionPageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageVersions(
                        request == null ? null : request.getTaskType(),
                        request == null ? null : request.getStatus(),
                        request == null ? null : request.getSourceContentType(),
                        request == null ? null : request.getSourceContentId(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询图谱版本详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱版本详情")
    @PostMapping("version/get")
    public GraphExtractionResponses.VersionResponse getVersionDetail(
            @Valid @RequestBody GraphExtractionRequests.VersionIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getVersionDetail(request == null ? null : request.getVersionId()));
    }

    @Operation(summary = "分页查询正式实体", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式实体分页")
    @PostMapping("entity/page")
    public PageResponse<GraphExtractionResponses.EntityResponse> pageEntities(
            @Valid @RequestBody GraphExtractionRequests.EntityPageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageEntities(
                        request == null ? null : request.getVersionId(),
                        request == null ? null : request.getKeyword(),
                        request == null ? null : request.getEntityType(),
                        request == null ? null : request.getConfirmationStatus(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询正式实体详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式实体详情")
    @PostMapping("entity/get")
    public GraphExtractionResponses.EntityResponse getEntityDetail(
            @Valid @RequestBody GraphExtractionRequests.EntityIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getEntityDetail(request == null ? null : request.getEntityId()));
    }

    @Operation(summary = "分页查询正式关系", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式关系分页")
    @PostMapping("relation/page")
    public PageResponse<GraphExtractionResponses.RelationResponse> pageRelations(
            @Valid @RequestBody GraphExtractionRequests.RelationPageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageRelations(
                        request == null ? null : request.getVersionId(),
                        request == null ? null : request.getKeyword(),
                        request == null ? null : request.getRelationType(),
                        request == null ? null : request.getConfirmationStatus(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询正式关系详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式关系详情")
    @PostMapping("relation/get")
    public GraphExtractionResponses.RelationResponse getRelationDetail(
            @Valid @RequestBody GraphExtractionRequests.RelationIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getRelationDetail(request == null ? null : request.getRelationId()));
    }

    @Operation(summary = "分页查询正式世系节点", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式世系节点分页")
    @PostMapping("lineage/node/page")
    public PageResponse<GraphExtractionResponses.LineageNodeResponse> pageLineageNodes(
            @Valid @RequestBody GraphExtractionRequests.LineageNodePageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageLineageNodes(
                        request == null ? null : request.getVersionId(),
                        request == null ? null : request.getKeyword(),
                        request == null ? null : request.getNodeType(),
                        request == null ? null : request.getConfirmationStatus(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询正式世系节点详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式世系节点详情")
    @PostMapping("lineage/node/get")
    public GraphExtractionResponses.LineageNodeResponse getLineageNodeDetail(
            @Valid @RequestBody GraphExtractionRequests.LineageNodeIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getLineageNodeDetail(request == null ? null : request.getNodeId()));
    }

    @Operation(summary = "分页查询正式世系关系", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式世系关系分页")
    @PostMapping("lineage/relation/page")
    public PageResponse<GraphExtractionResponses.LineageRelationResponse> pageLineageRelations(
            @Valid @RequestBody GraphExtractionRequests.LineageRelationPageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageLineageRelations(
                        request == null ? null : request.getVersionId(),
                        request == null ? null : request.getKeyword(),
                        request == null ? null : request.getRelationType(),
                        request == null ? null : request.getConfirmationStatus(),
                        PageInterfaceAssembler.toPageQuery(request)),
                KnowledgeGraphExtractionInterfaceAssembler::toResponse);
    }

    @Operation(summary = "查询正式世系关系详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "正式世系关系详情")
    @PostMapping("lineage/relation/get")
    public GraphExtractionResponses.LineageRelationResponse getLineageRelationDetail(
            @Valid @RequestBody GraphExtractionRequests.LineageRelationIdRequest request) {
        return KnowledgeGraphExtractionInterfaceAssembler.toResponse(
                extractionService.getLineageRelationDetail(request == null ? null : request.getRelationId()));
    }
}
