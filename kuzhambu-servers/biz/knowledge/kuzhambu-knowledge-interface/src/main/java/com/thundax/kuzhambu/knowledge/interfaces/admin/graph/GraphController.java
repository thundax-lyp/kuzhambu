package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.security.context.KuzhambuContextHolder;
import com.thundax.kuzhambu.common.security.token.AccessTokenNames;
import com.thundax.kuzhambu.common.web.annotation.SysLogger;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublishedApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.GraphInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphExtractionRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublishedRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphMaterialResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublicationResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphPublishedResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识模块-图谱管理", description = "双空间知识图谱管理接口")
@SysLogger(module = {"知识", "图谱管理"})
@RequestMapping("/api/knowledge/graph")
@WrappedApiController
public class GraphController {
    private final GraphWorkbenchApplicationService workbenchService;
    private final GraphMaterialApplicationService materialService;
    private final GraphExtractionApplicationService extractionService;
    private final GraphPublicationApplicationService publicationService;
    private final GraphPublishedApplicationService publishedService;

    public GraphController(
            GraphWorkbenchApplicationService workbenchService,
            GraphMaterialApplicationService materialService,
            GraphExtractionApplicationService extractionService,
            GraphPublicationApplicationService publicationService,
            GraphPublishedApplicationService publishedService) {
        this.workbenchService = workbenchService;
        this.materialService = materialService;
        this.extractionService = extractionService;
        this.publicationService = publicationService;
        this.publishedService = publishedService;
    }

    @Operation(summary = "获取图谱工作台统计", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱工作台统计")
    @PostMapping("workbench/overview/get")
    public GraphWorkbenchResponses.OverviewData overview(
            @Valid @RequestBody GraphWorkbenchRequests.OverviewGetRequest request) {
        var result = workbenchService.getOverview();
        return new GraphWorkbenchResponses.OverviewData(
                String.valueOf(result.publishedNodeCount()),
                String.valueOf(result.publishedEdgeCount()),
                String.valueOf(result.coveredMaterialCount()),
                String.valueOf(result.isolatedNodeCount()),
                String.valueOf(result.missingCoreRelationNodeCount()),
                result.recentActivities().stream()
                        .map(GraphInterfaceAssembler::toActivityData)
                        .toList(),
                String.valueOf(result.pendingConflictCount()));
    }

    @Operation(summary = "查询图谱种子节点", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱种子节点")
    @PostMapping("workbench/seeds/list")
    public GraphWorkbenchResponses.SeedsData seeds(
            @Valid @RequestBody GraphWorkbenchRequests.SeedsListRequest request) {
        return new GraphWorkbenchResponses.SeedsData(workbenchService.listRecentSeedNodes().stream()
                .map(GraphInterfaceAssembler::toNodeData)
                .toList());
    }

    @Operation(summary = "查询图谱邻接关系", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱邻接关系")
    @PostMapping("workbench/incident-edges/list")
    public GraphWorkbenchResponses.IncidentEdgesData incidentEdges(
            @Valid @RequestBody GraphWorkbenchRequests.IncidentEdgesListRequest request) {
        var result = workbenchService.listIncidentEdges(
                GraphInterfaceAssembler.toQuery(request),
                PageInterfaceAssembler.toPageQuery(null, request.getPageSize()));
        return new GraphWorkbenchResponses.IncidentEdgesData(
                result.nodes().stream().map(GraphInterfaceAssembler::toNodeData).toList(),
                result.edges().stream().map(GraphInterfaceAssembler::toEdgeData).toList(),
                result.nextCursor() == null
                        ? null
                        : String.valueOf(result.nextCursor().value()),
                result.truncated());
    }

    @Operation(summary = "分页搜索图谱对象", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱对象搜索")
    @PostMapping("workbench/search/page")
    public PageResponse<GraphWorkbenchResponses.SearchData> search(
            @Valid @RequestBody GraphWorkbenchRequests.SearchPageRequest request) {
        var result = workbenchService.search(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toSearchData);
    }

    @Operation(summary = "获取图谱质量待办", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱质量待办")
    @PostMapping("workbench/quality/get")
    public GraphWorkbenchResponses.QualityData quality(
            @Valid @RequestBody GraphWorkbenchRequests.QualityGetRequest request) {
        var result = workbenchService.getQuality(GraphInterfaceAssembler.toQuery(request));
        return new GraphWorkbenchResponses.QualityData(
                String.valueOf(result.isolatedNodeCount()),
                String.valueOf(result.missingCoreRelationNodeCount()),
                result.isolatedNodes().stream()
                        .map(GraphInterfaceAssembler::toNodeData)
                        .toList(),
                result.missingCoreRelationNodes().stream()
                        .map(GraphInterfaceAssembler::toNodeData)
                        .toList());
    }

    private PageQuery pageQuery(String pageNo, String pageSize) {
        return PageInterfaceAssembler.toPageQuery(
                pageNo == null ? null : Integer.valueOf(pageNo), pageSize == null ? null : Integer.valueOf(pageSize));
    }

    private Long currentSubjectLong() {
        String subjectId = KuzhambuContextHolder.currentSubjectId();
        if (subjectId == null || subjectId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subjectId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Operation(summary = "分页查询图谱素材", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱素材分页")
    @PostMapping("material/page")
    public PageResponse<GraphMaterialResponses.MaterialPageData> materialPage(
            @Valid @RequestBody GraphMaterialRequests.MaterialPageRequest request) {
        var result = materialService.pageMaterials(
                GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId()),
                pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toMaterialPageData);
    }

    @Operation(summary = "获取图谱素材画布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱素材详情")
    @PostMapping("material/get")
    public GraphMaterialResponses.DetailData materialGet(
            @Valid @RequestBody GraphMaterialRequests.ContentRefRequest request) {
        var result = materialService.getMaterialGraph(
                GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId()));
        return GraphInterfaceAssembler.toDetailData(result, List.of());
    }

    @Operation(summary = "创建图谱素材提取任务", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取创建")
    @PostMapping("material/extraction/create")
    public GraphExtractionResponses.TaskData extractionCreate(
            @Valid @RequestBody GraphExtractionRequests.ExtractionCreateRequest request) {
        return GraphInterfaceAssembler.toTaskData(
                extractionService.createExtraction(GraphInterfaceAssembler.toCommand(request, currentSubjectLong())));
    }

    @Operation(summary = "批量创建图谱提取任务", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱批量提取创建")
    @PostMapping("task/batch/create")
    public GraphExtractionResponses.BatchResultData taskBatchCreate(
            @Valid @RequestBody GraphExtractionRequests.BatchCreateRequest request) {
        return GraphInterfaceAssembler.toBatchData(extractionService.createBatchExtraction(
                GraphInterfaceAssembler.toCommand(request, currentSubjectLong())));
    }

    @Operation(summary = "分页查询图谱提取任务", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱提取任务分页")
    @PostMapping("task/page")
    public PageResponse<GraphExtractionResponses.TaskData> taskPage(
            @Valid @RequestBody GraphExtractionRequests.TaskPageRequest request) {
        return PageResponseHelper.fromPageResult(
                extractionService.pageTasks(
                        GraphInterfaceAssembler.toQuery(request),
                        pageQuery(request.getPageNo(), request.getPageSize())),
                GraphInterfaceAssembler::toTaskData);
    }

    @Operation(summary = "获取图谱提取任务详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱提取任务详情")
    @PostMapping("task/get")
    public GraphExtractionResponses.TaskDetailData taskGet(
            @Valid @RequestBody GraphExtractionRequests.TaskGetRequest request) {
        return GraphInterfaceAssembler.toTaskDetailData(
                extractionService.getTask(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "重试图谱提取任务", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取任务重试")
    @PostMapping("task/retry")
    public GraphExtractionResponses.TaskData taskRetry(
            @Valid @RequestBody GraphExtractionRequests.TaskActionRequest request) {
        return GraphInterfaceAssembler.toTaskData(
                extractionService.retryTask(GraphInterfaceAssembler.toRetryCommand(request)));
    }

    @Operation(summary = "取消图谱提取任务", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取任务取消")
    @PostMapping("task/cancel")
    public GraphExtractionResponses.TaskData taskCancel(
            @Valid @RequestBody GraphExtractionRequests.TaskActionRequest request) {
        return GraphInterfaceAssembler.toTaskData(
                extractionService.cancelTask(GraphInterfaceAssembler.toCancelCommand(request)));
    }

    @Operation(summary = "采用图谱提取候选", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取候选采用")
    @PostMapping("task/candidate/apply")
    public GraphExtractionResponses.CandidateApplyData candidateApply(
            @Valid @RequestBody GraphExtractionRequests.CandidateApplyRequest request) {
        return GraphInterfaceAssembler.toCandidateApplyData(
                extractionService.applyCandidate(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "丢弃图谱提取候选", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取候选丢弃")
    @PostMapping("task/candidate/discard")
    public GraphExtractionResponses.TaskData candidateDiscard(
            @Valid @RequestBody GraphExtractionRequests.CandidateDiscardRequest request) {
        return GraphInterfaceAssembler.toTaskData(
                extractionService.discardCandidate(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "重新生成图谱提取候选", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱提取候选重新生成")
    @PostMapping("task/candidate/regenerate")
    public GraphExtractionResponses.TaskData candidateRegenerate(
            @Valid @RequestBody GraphExtractionRequests.CandidateRegenerateRequest request) {
        return GraphInterfaceAssembler.toTaskData(
                extractionService.regenerateTask(GraphInterfaceAssembler.toCommand(request, currentSubjectLong())));
    }

    @Operation(summary = "创建图谱素材节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材节点创建")
    @PostMapping("material/node/create")
    public GraphMaterialResponses.DetailData materialNodeCreate(
            @Valid @RequestBody GraphMaterialRequests.MaterialObjectRequest request) {
        materialService.createNode(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "更新图谱素材节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材节点更新")
    @PostMapping("material/node/update")
    public GraphMaterialResponses.DetailData materialNodeUpdate(
            @Valid @RequestBody GraphMaterialRequests.MaterialObjectRequest request) {
        materialService.updateNode(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "删除图谱素材节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材节点删除")
    @PostMapping("material/node/delete")
    public GraphMaterialResponses.DetailData materialNodeDelete(
            @Valid @RequestBody GraphMaterialRequests.MaterialObjectDeleteRequest request) {
        materialService.deleteNode(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "创建图谱素材关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材关系创建")
    @PostMapping("material/edge/create")
    public GraphMaterialResponses.DetailData materialEdgeCreate(
            @Valid @RequestBody GraphMaterialRequests.MaterialEdgeRequest request) {
        materialService.createEdge(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "更新图谱素材关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材关系更新")
    @PostMapping("material/edge/update")
    public GraphMaterialResponses.DetailData materialEdgeUpdate(
            @Valid @RequestBody GraphMaterialRequests.MaterialEdgeRequest request) {
        materialService.updateEdge(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "删除图谱素材关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材关系删除")
    @PostMapping("material/edge/delete")
    public GraphMaterialResponses.DetailData materialEdgeDelete(
            @Valid @RequestBody GraphMaterialRequests.MaterialEdgeDeleteRequest request) {
        materialService.deleteEdge(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toDetailData(
                materialService.getMaterialGraph(
                        GraphInterfaceAssembler.toQuery(request, KuzhambuContextHolder.currentSubjectId())),
                List.of());
    }

    @Operation(summary = "预览图谱素材节点合并", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱素材节点合并预览")
    @PostMapping("material/node/merge/preview")
    public GraphMaterialResponses.ChangeImpactData materialNodeMergePreview(
            @Valid @RequestBody GraphMaterialRequests.MaterialNodeMergePreviewRequest request) {
        return GraphInterfaceAssembler.toChangeImpactData(
                materialService.previewNodeMerge(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "应用图谱素材节点合并", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材节点合并")
    @PostMapping("material/node/merge/apply")
    public GraphMaterialResponses.DetailData materialNodeMergeApply(
            @Valid @RequestBody GraphMaterialRequests.MaterialNodeMergeApplyRequest request) {
        return GraphInterfaceAssembler.toDetailData(
                materialService.mergeNodes(GraphInterfaceAssembler.toCommand(request)), List.of());
    }

    @Operation(summary = "预览图谱素材节点拆分", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱素材节点拆分预览")
    @PostMapping("material/node/split/preview")
    public GraphMaterialResponses.ChangeImpactData materialNodeSplitPreview(
            @Valid @RequestBody GraphMaterialRequests.MaterialNodeSplitPreviewRequest request) {
        return GraphInterfaceAssembler.toChangeImpactData(
                materialService.previewNodeSplit(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "应用图谱素材节点拆分", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材节点拆分")
    @PostMapping("material/node/split/apply")
    public GraphMaterialResponses.DetailData materialNodeSplitApply(
            @Valid @RequestBody GraphMaterialRequests.MaterialNodeSplitApplyRequest request) {
        return GraphInterfaceAssembler.toDetailData(
                materialService.splitNode(GraphInterfaceAssembler.toCommand(request)), List.of());
    }

    @Operation(summary = "预览导入图谱素材 JSON", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱素材导入预览")
    @PostMapping("material/import/preview")
    public GraphMaterialResponses.ImportPreviewData materialImportPreview(
            @Valid @RequestBody GraphMaterialRequests.MaterialImportPreviewRequest request) {
        return GraphInterfaceAssembler.toImportPreviewData(
                materialService.previewImport(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "应用导入图谱素材 JSON", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材导入")
    @PostMapping("material/import/apply")
    public GraphMaterialResponses.DetailData materialImportApply(
            @Valid @RequestBody GraphMaterialRequests.MaterialImportApplyRequest request) {
        return GraphInterfaceAssembler.toDetailData(
                materialService.importGraph(GraphInterfaceAssembler.toCommand(request)), List.of());
    }

    @Operation(summary = "导出图谱素材 JSON", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱素材导出")
    @PostMapping("material/export")
    public GraphMaterialResponses.ExportData materialExport(
            @Valid @RequestBody GraphMaterialRequests.ContentRefRequest request) {
        return new GraphMaterialResponses.ExportData(
                "graph.json",
                java.util.Map.of("graphJson", materialService.exportGraph(GraphInterfaceAssembler.toQuery(request))));
    }

    @Operation(summary = "预览图谱素材发布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱发布预览")
    @PostMapping("publication/preview")
    public GraphPublicationResponses.PreviewData publicationPreview(
            @Valid @RequestBody GraphPublicationRequests.PublicationPreviewRequest request) {
        return GraphInterfaceAssembler.toPreviewData(
                publicationService.previewPublication(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "确认发布图谱素材", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱发布确认")
    @PostMapping("publication/publish")
    public GraphPublicationResponses.PublicationData publicationPublish(
            @Valid @RequestBody GraphPublicationRequests.PublicationConfirmRequest request) {
        return GraphInterfaceAssembler.toPublicationData(
                publicationService.publish(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "批量预览图谱素材发布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱批量发布预览")
    @PostMapping("publication/batch/preview")
    public GraphPublicationResponses.BatchPreviewData publicationBatchPreview(
            @Valid @RequestBody GraphPublicationRequests.BatchPublicationPreviewRequest request) {
        return GraphInterfaceAssembler.toBatchPreviewData(
                publicationService.previewBatchPublication(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "批量确认图谱素材发布", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱批量发布确认")
    @PostMapping("publication/batch/publish")
    public GraphPublicationResponses.BatchData publicationBatchPublish(
            @Valid @RequestBody GraphPublicationRequests.BatchPublicationConfirmRequest request) {
        return GraphInterfaceAssembler.toBatchData(
                publicationService.publishBatch(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "预览撤回图谱素材发布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱发布撤回预览")
    @PostMapping("publication/withdrawal/preview")
    public GraphPublicationResponses.WithdrawalPreviewData withdrawalPreview(
            @Valid @RequestBody GraphPublicationRequests.WithdrawalRequest request) {
        return GraphInterfaceAssembler.toWithdrawalPreviewData(
                publicationService.previewWithdrawal(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "撤回图谱素材发布", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱发布撤回")
    @PostMapping("publication/withdrawal/withdraw")
    public GraphMaterialResponses.MaterialData withdrawal(
            @Valid @RequestBody GraphPublicationRequests.WithdrawalRequest request) {
        return GraphInterfaceAssembler.toMaterialData(
                publicationService.withdraw(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "批量预览撤回图谱素材发布", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "图谱批量发布撤回预览")
    @PostMapping("publication/batch/withdrawal/preview")
    public GraphPublicationResponses.BatchWithdrawalPreviewData withdrawalBatchPreview(
            @Valid @RequestBody GraphPublicationRequests.BatchWithdrawalPreviewRequest request) {
        return GraphInterfaceAssembler.toBatchWithdrawalPreviewData(
                publicationService.previewBatchWithdrawal(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "批量撤回图谱素材发布", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "图谱批量发布撤回")
    @PostMapping("publication/batch/withdrawal/withdraw")
    public GraphPublicationResponses.BatchWithdrawalData withdrawalBatch(
            @Valid @RequestBody GraphPublicationRequests.BatchWithdrawalRequest request) {
        return GraphInterfaceAssembler.toBatchWithdrawalData(
                publicationService.withdrawBatch(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询发布节点", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布节点分页")
    @PostMapping("published/node/page")
    public PageResponse<GraphPublishedResponses.NodeData> publishedNodePage(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodePageRequest request) {
        var result = publishedService.pageNodes(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toNodeData);
    }

    @Operation(summary = "获取发布节点详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布节点详情")
    @PostMapping("published/node/get")
    public GraphPublishedResponses.NodeDetailData publishedNodeGet(
            @Valid @RequestBody GraphPublishedRequests.PublishedIdRequest request) {
        return GraphInterfaceAssembler.toNodeDetailData(
                publishedService.getNodeDetail(GraphInterfaceAssembler.toNodeId(request.getNodeId())));
    }

    @Operation(summary = "创建发布节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布节点创建")
    @PostMapping("published/node/create")
    public GraphPublishedResponses.NodeDetailData publishedNodeCreate(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeSaveRequest request) {
        var nodeId = publishedService.createNode(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toNodeDetailData(publishedService.getNodeDetail(nodeId));
    }

    @Operation(summary = "更新发布节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布节点更新")
    @PostMapping("published/node/update")
    public GraphPublishedResponses.NodeDetailData publishedNodeUpdate(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeSaveRequest request) {
        publishedService.updateNode(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toNodeDetailData(publishedService.getNodeDetail(
                GraphInterfaceAssembler.toNodeId(request.getNode().getId())));
    }

    @Operation(summary = "预览发布节点删除影响", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布节点删除预览")
    @PostMapping("published/node/delete/preview")
    public GraphPublishedResponses.GovernanceImpactData publishedNodeDeletePreview(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeDeletePreviewRequest request) {
        return GraphInterfaceAssembler.toGovernanceImpactData(
                publishedService.previewNodeDeletion(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "删除发布节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布节点删除")
    @PostMapping("published/node/delete")
    public GraphPublishedResponses.NodeData publishedNodeDelete(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeDeleteRequest request) {
        publishedService.deleteNode(GraphInterfaceAssembler.toCommand(request));
        return new GraphPublishedResponses.NodeData(request.getNodeId(), null, null, null, "DELETED", null);
    }

    @Operation(summary = "预览发布节点合并影响", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布节点合并预览")
    @PostMapping("published/node/merge/preview")
    public GraphPublishedResponses.GovernanceImpactData publishedNodeMergePreview(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeMergePreviewRequest request) {
        return GraphInterfaceAssembler.toGovernanceImpactData(
                publishedService.previewNodeMerge(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "合并发布节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布节点合并")
    @PostMapping("published/node/merge")
    public GraphPublishedResponses.NodeDetailData publishedNodeMerge(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeMergeRequest request) {
        return GraphInterfaceAssembler.toNodeDetailData(
                publishedService.mergeNodes(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "预览发布节点拆分影响", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布节点拆分预览")
    @PostMapping("published/node/split/preview")
    public GraphPublishedResponses.GovernanceImpactData publishedNodeSplitPreview(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeSplitPreviewRequest request) {
        return GraphInterfaceAssembler.toGovernanceImpactData(
                publishedService.previewNodeSplit(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "拆分发布节点", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布节点拆分")
    @PostMapping("published/node/split")
    public GraphPublishedResponses.NodeDetailData publishedNodeSplit(
            @Valid @RequestBody GraphPublishedRequests.PublishedNodeSplitRequest request) {
        return GraphInterfaceAssembler.toNodeDetailData(
                publishedService.splitNode(GraphInterfaceAssembler.toCommand(request)));
    }

    @Operation(summary = "分页查询发布关系", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布关系分页")
    @PostMapping("published/edge/page")
    public PageResponse<GraphPublishedResponses.EdgeData> publishedEdgePage(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgePageRequest request) {
        var result = publishedService.pageEdges(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toEdgeData);
    }

    @Operation(summary = "分页查询发布空间单跳邻接表", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布邻接分页")
    @PostMapping("published/adjacency/page")
    public PageResponse<GraphPublishedResponses.AdjacencyData> publishedAdjacencyPage(
            @Valid @RequestBody GraphPublishedRequests.PublishedAdjacencyPageRequest request) {
        var result = publishedService.pageAdjacency(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toAdjacencyData);
    }

    @Operation(summary = "获取发布关系详情", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布关系详情")
    @PostMapping("published/edge/get")
    public GraphPublishedResponses.EdgeDetailData publishedEdgeGet(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgeIdRequest request) {
        return GraphInterfaceAssembler.toEdgeDetailData(
                publishedService.getEdgeDetail(GraphInterfaceAssembler.toEdgeId(request.getEdgeId())));
    }

    @Operation(summary = "创建发布关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布关系创建")
    @PostMapping("published/edge/create")
    public GraphPublishedResponses.EdgeDetailData publishedEdgeCreate(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        var edgeId = publishedService.createEdge(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toEdgeDetailData(publishedService.getEdgeDetail(edgeId));
    }

    @Operation(summary = "更新发布关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布关系更新")
    @PostMapping("published/edge/update")
    public GraphPublishedResponses.EdgeDetailData publishedEdgeUpdate(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgeSaveRequest request) {
        publishedService.updateEdge(GraphInterfaceAssembler.toCommand(request));
        return GraphInterfaceAssembler.toEdgeDetailData(publishedService.getEdgeDetail(
                GraphInterfaceAssembler.toEdgeId(request.getEdge().getId())));
    }

    @Operation(summary = "预览发布关系删除影响", description = "knowledge:graph:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:view")
    @SysLogger(value = "发布关系删除预览")
    @PostMapping("published/edge/delete/preview")
    public GraphPublishedResponses.GovernanceImpactData publishedEdgeDeletePreview(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgeIdRequest request) {
        return GraphInterfaceAssembler.toGovernanceImpactData(
                publishedService.previewEdgeDeletion(GraphInterfaceAssembler.toQuery(request)));
    }

    @Operation(summary = "删除发布关系", description = "knowledge:graph:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = AccessTokenNames.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @HasPermission("knowledge:graph:edit")
    @SysLogger(value = "发布关系删除")
    @PostMapping("published/edge/delete")
    public GraphPublishedResponses.EdgeData publishedEdgeDelete(
            @Valid @RequestBody GraphPublishedRequests.PublishedEdgeDeleteRequest request) {
        publishedService.deleteEdge(GraphInterfaceAssembler.toCommand(request));
        return new GraphPublishedResponses.EdgeData(request.getEdgeId(), null, null, null, null, null, "DELETED", null);
    }
}
