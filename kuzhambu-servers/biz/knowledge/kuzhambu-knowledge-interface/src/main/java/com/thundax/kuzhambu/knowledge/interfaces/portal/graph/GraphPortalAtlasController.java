package com.thundax.kuzhambu.knowledge.interfaces.portal.graph;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.assembler.GraphPortalAtlasInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalAtlasRequests;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response.GraphPortalAtlasResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@PublicApi
@RequestMapping("/api/portal/knowledge/graph/atlas")
@WrappedApiController
public class GraphPortalAtlasController {
    private final GraphWorkbenchApplicationService service;

    public GraphPortalAtlasController(GraphWorkbenchApplicationService service) {
        this.service = service;
    }

    @PostMapping("overview/get")
    public GraphPortalAtlasResponses.OverviewData overview() {
        var value = service.getOverview();
        return new GraphPortalAtlasResponses.OverviewData(
                String.valueOf(value.publishedNodeCount()), String.valueOf(value.publishedEdgeCount()),
                String.valueOf(value.coveredMaterialCount()), String.valueOf(value.isolatedNodeCount()));
    }

    @PostMapping("recent-edges/list")
    public GraphPortalAtlasResponses.GraphData recentEdges() {
        var value = service.listRecentEdges();
        return new GraphPortalAtlasResponses.GraphData(
                value.nodes().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toEdgeData)
                        .toList());
    }

    @PostMapping("one-hop-edges/list")
    public GraphPortalAtlasResponses.OneHopEdgesData oneHopEdges(
            @Valid @RequestBody GraphPortalAtlasRequests.OneHopEdgesListRequest request) {
        var value = service.listOneHopEdges(GraphPortalAtlasInterfaceAssembler.toQuery(request));
        return new GraphPortalAtlasResponses.OneHopEdgesData(
                value.nodes().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toEdgeData)
                        .toList(),
                value.nextCursor() == null
                        ? null
                        : String.valueOf(value.nextCursor().value()),
                value.truncated());
    }
}
