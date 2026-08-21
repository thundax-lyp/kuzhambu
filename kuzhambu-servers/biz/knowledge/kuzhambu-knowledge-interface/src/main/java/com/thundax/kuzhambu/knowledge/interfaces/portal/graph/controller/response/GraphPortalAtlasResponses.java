package com.thundax.kuzhambu.knowledge.interfaces.portal.graph.controller.response;

import java.util.List;

public final class GraphPortalAtlasResponses {
    private GraphPortalAtlasResponses() {}

    public record OverviewData(
            String publishedNodeCount,
            String publishedEdgeCount,
            String coveredMaterialCount,
            String isolatedNodeCount) {}

    public record NodeData(String id, String nodeType, String name) {}

    public record EdgeData(String id, String sourceNodeId, String targetNodeId, String relationType) {}

    public record GraphData(List<NodeData> nodes, List<EdgeData> edges) {}

    public record OneHopEdgesData(List<NodeData> nodes, List<EdgeData> edges, String nextCursor, boolean truncated) {}
}
