package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** 图谱工作台 HTTP 响应契约。 */
public final class GraphWorkbenchResponses {
    private GraphWorkbenchResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ActivityData(
            String type, GraphMaterialResponses.ContentRefData contentRef, String occurredAt, String summary) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OverviewData(
            String snapshotAt,
            String publishedNodeCount,
            String publishedEdgeCount,
            String coveredMaterialCount,
            String isolatedNodeCount,
            String missingCoreRelationNodeCount,
            List<ActivityData> recentActivities,
            String pendingConflictCount) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record RecentEdgesData(
            List<GraphPublishedResponses.NodeData> nodes, List<GraphPublishedResponses.EdgeData> edges) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record IncidentEdgesData(
            List<GraphPublishedResponses.NodeData> nodes,
            List<GraphPublishedResponses.EdgeData> edges,
            String nextCursor,
            boolean truncated) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SearchData(
            String objectType, GraphPublishedResponses.NodeData node, GraphPublishedResponses.EdgeData edge) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PageData<T>(String pageNo, String pageSize, String totalCount, String totalPage, List<T> records) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record QualityData(
            String isolatedNodeCount,
            String missingCoreRelationNodeCount,
            List<GraphPublishedResponses.NodeData> isolatedNodes,
            List<GraphPublishedResponses.NodeData> missingCoreRelationNodes) {}
}
