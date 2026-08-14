package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.security.annotation.HasPermission;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.common.web.assembler.PageInterfaceAssembler;
import com.thundax.kuzhambu.common.web.response.PageResponse;
import com.thundax.kuzhambu.common.web.response.PageResponseHelper;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.assembler.GraphInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.request.GraphWorkbenchRequests;
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

    public GraphController(GraphWorkbenchApplicationService workbenchService) {
        this.workbenchService = workbenchService;
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
}
