package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/** 图谱素材 HTTP 响应契约。 */
public final class GraphMaterialResponses {
    private GraphMaterialResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ContentRefData(String contentType, String contentRefId) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record MaterialData(
            String id,
            ContentRefData contentRef,
            String contentTitleSnapshot,
            String status,
            String lockVersion,
            String publishedAt,
            String failureReason,
            String failedOperation) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SourceData(
            ContentRefData contentRef,
            String title,
            String summary,
            String contentType,
            String categoryCode,
            String categoryName,
            String volumeCode,
            String volumeName,
            boolean graphable) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record MaterialStatsData(
            String materialId,
            String draftNodeCount,
            String draftEdgeCount,
            String publishedNodeCount,
            String publishedEdgeCount,
            String activeTaskCount,
            String pendingReviewTaskCount,
            String failedTaskCount,
            String statsRevision,
            String calculatedAt) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record MaterialPageData(
            SourceData source,
            MaterialData material,
            MaterialStatsData materialStats,
            com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses.TaskData
                    latestTask) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record NodeData(String id, String nodeType, String name, Map<String, Object> properties, String source) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record EdgeData(
            String id,
            String sourceNodeId,
            String targetNodeId,
            String relationType,
            Map<String, Object> qualifiers,
            String source) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record IssueData(
            String code, String severity, String objectType, String objectId, String field, String message) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GraphData(MaterialData material, List<NodeData> nodes, List<EdgeData> edges) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record TaskData(
            String id,
            String status,
            String progress,
            String inputSnapshotVersion,
            String resultSummary,
            String failureReason,
            String retryFromTaskId,
            String requestedAt,
            String completedAt) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record DetailData(
            SourceData source,
            MaterialData material,
            MaterialStatsData materialStats,
            List<NodeData> nodes,
            List<EdgeData> edges,
            com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response.GraphExtractionResponses.TaskData
                    taskSummary,
            List<TaskData> extractionTasks) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ChangeImpactData(
            List<NodeData> nodes, List<EdgeData> edges, List<IssueData> issues, boolean executable) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ImportPreviewData(
            GraphData importedGraph,
            int createdNodeCount,
            int updatedNodeCount,
            int createdEdgeCount,
            int updatedEdgeCount,
            List<IssueData> issues,
            boolean importable) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ExportData(String fileName, Map<String, Object> graphJson) {}
}
