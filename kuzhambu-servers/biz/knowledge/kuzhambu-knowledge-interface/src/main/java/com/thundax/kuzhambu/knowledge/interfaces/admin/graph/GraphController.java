package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPublicationApplicationService;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.GraphInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphMaterialRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphPublicationRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphMaterialResponses;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphWorkbenchResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/knowledge/graph")
@WrappedApiController
public class GraphController {
    private final GraphWorkbenchApplicationService workbenchService;
    private final GraphMaterialApplicationService materialService;
    private final GraphPublicationApplicationService publicationService;

    public GraphController(
            GraphWorkbenchApplicationService workbenchService,
            GraphMaterialApplicationService materialService,
            GraphPublicationApplicationService publicationService) {
        this.workbenchService = workbenchService;
        this.materialService = materialService;
        this.publicationService = publicationService;
    }

    @HasPermission("knowledge:graph:view")
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
                List.of(),
                "0");
    }

    @HasPermission("knowledge:graph:view")
    @PostMapping("workbench/seeds/list")
    public GraphWorkbenchResponses.SeedsData seeds(
            @Valid @RequestBody GraphWorkbenchRequests.SeedsListRequest request) {
        return new GraphWorkbenchResponses.SeedsData(workbenchService.listRecentSeedNodes().stream()
                .map(GraphInterfaceAssembler::toNodeData)
                .toList());
    }

    @HasPermission("knowledge:graph:view")
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

    @HasPermission("knowledge:graph:view")
    @PostMapping("workbench/search/page")
    public PageResponse<GraphWorkbenchResponses.SearchData> search(
            @Valid @RequestBody GraphWorkbenchRequests.SearchPageRequest request) {
        var result = workbenchService.search(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toSearchData);
    }

    @HasPermission("knowledge:graph:view")
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

    @HasPermission("knowledge:graph:view")
    @PostMapping("material/page")
    public PageResponse<GraphMaterialResponses.MaterialData> materialPage(
            @Valid @RequestBody GraphMaterialRequests.MaterialPageRequest request) {
        var result = materialService.pageMaterials(
                GraphInterfaceAssembler.toQuery(request), pageQuery(request.getPageNo(), request.getPageSize()));
        return PageResponseHelper.fromPageResult(result, GraphInterfaceAssembler::toMaterialData);
    }

    @HasPermission("knowledge:graph:view")
    @PostMapping("material/get")
    public GraphMaterialResponses.DetailData materialGet(
            @Valid @RequestBody GraphMaterialRequests.ContentRefRequest request) {
        var result = materialService.getMaterialGraph(GraphInterfaceAssembler.toQuery(request));
        return new GraphMaterialResponses.DetailData(
                GraphInterfaceAssembler.toMaterialData(result.material()), List.of(), List.of(), List.of());
    }

    @HasPermission("knowledge:graph:edit")
    @PostMapping("material/export")
    public GraphMaterialResponses.ExportData materialExport(
            @Valid @RequestBody GraphMaterialRequests.ContentRefRequest request) {
        return new GraphMaterialResponses.ExportData(
                "graph.json",
                java.util.Map.of("graphJson", materialService.exportGraph(GraphInterfaceAssembler.toQuery(request))));
    }

    @HasPermission("knowledge:graph:view")
    @PostMapping("publication/preview")
    public Object publicationPreview(@Valid @RequestBody GraphPublicationRequests.PublicationPreviewRequest request) {
        return publicationService.previewPublication(GraphInterfaceAssembler.toQuery(request));
    }

    @HasPermission("knowledge:graph:edit")
    @PostMapping("publication/publish")
    public Object publicationPublish(@Valid @RequestBody GraphPublicationRequests.PublicationConfirmRequest request) {
        return publicationService.publish(GraphInterfaceAssembler.toCommand(request));
    }

    @HasPermission("knowledge:graph:view")
    @PostMapping("publication/batch/preview")
    public Object publicationBatchPreview(
            @Valid @RequestBody GraphPublicationRequests.BatchPublicationPreviewRequest request) {
        return publicationService.previewBatchPublication(GraphInterfaceAssembler.toQuery(request));
    }

    @HasPermission("knowledge:graph:edit")
    @PostMapping("publication/batch/publish")
    public Object publicationBatchPublish(
            @Valid @RequestBody GraphPublicationRequests.BatchPublicationConfirmRequest request) {
        return publicationService.publishBatch(GraphInterfaceAssembler.toCommand(request));
    }

    @HasPermission("knowledge:graph:view")
    @PostMapping("publication/withdrawal/preview")
    public Object withdrawalPreview(@Valid @RequestBody GraphPublicationRequests.WithdrawalRequest request) {
        return publicationService.previewWithdrawal(GraphInterfaceAssembler.toQuery(request));
    }

    @HasPermission("knowledge:graph:edit")
    @PostMapping("publication/withdrawal/withdraw")
    public Object withdrawal(@Valid @RequestBody GraphPublicationRequests.WithdrawalRequest request) {
        return publicationService.withdraw(GraphInterfaceAssembler.toCommand(request));
    }
}
