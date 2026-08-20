package com.thundax.kuzhambu.knowledge.interfaces.portal.graph;

import com.thundax.kuzhambu.common.security.annotation.PublicApi;
import com.thundax.kuzhambu.common.web.annotation.WrappedApiController;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphPortalApplicationService;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.assembler.GraphPortalAtlasInterfaceAssembler;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.request.GraphPortalAtlasRequests;
import com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response.GraphPortalAtlasResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "知识门户-图谱总谱", description = "门户图谱总览与关系探索")
@PublicApi
@RequestMapping("/api/portal/knowledge/graph/atlas")
@WrappedApiController
public class GraphPortalAtlasController {
    private final GraphPortalApplicationService portalService;

    public GraphPortalAtlasController(GraphPortalApplicationService portalService) {
        this.portalService = portalService;
    }

    @Operation(summary = "查询门户图谱总览", description = "公开访问")
    @PostMapping("overview/get")
    public GraphPortalAtlasResponses.OverviewData overview() {
        var value = portalService.getOverview();
        return new GraphPortalAtlasResponses.OverviewData(
                String.valueOf(value.publishedNodeCount()), String.valueOf(value.publishedEdgeCount()),
                String.valueOf(value.coveredMaterialCount()), String.valueOf(value.isolatedNodeCount()));
    }

    @Operation(summary = "查询门户图谱最近关系", description = "公开访问")
    @PostMapping("recent-edges/list")
    public GraphPortalAtlasResponses.GraphData recentEdges() {
        var value = portalService.listRecentEdges();
        return new GraphPortalAtlasResponses.GraphData(
                value.nodes().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toNodeData)
                        .toList(),
                value.edges().stream()
                        .map(GraphPortalAtlasInterfaceAssembler::toEdgeData)
                        .toList());
    }

    @Operation(summary = "查询门户图谱一跳关系", description = "公开访问")
    @PostMapping("one-hop-edges/list")
    public GraphPortalAtlasResponses.OneHopEdgesData oneHopEdges(
            @Valid @RequestBody GraphPortalAtlasRequests.OneHopEdgesListRequest request) {
        var value = portalService.listOneHopEdges(GraphPortalAtlasInterfaceAssembler.toQuery(request));
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
