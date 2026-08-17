package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** 图谱发布 HTTP 响应契约。 */
public final class GraphPublicationResponses {
    private GraphPublicationResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record MatchData(
            String materialObjectId,
            String matchType,
            String matchedObjectId,
            String matchedObjectLockVersion,
            List<GraphMaterialResponses.IssueData> issues) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PreviewData(
            String previewToken,
            GraphMaterialResponses.ContentRefData materialRef,
            String materialLockVersion,
            List<MatchData> nodes,
            List<MatchData> edges,
            List<GraphMaterialResponses.IssueData> issues,
            boolean publishable) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PublicationData(
            GraphMaterialResponses.ContentRefData contentRef,
            String materialStatus,
            boolean success,
            String failureMessage,
            int createdNodeCount,
            int reusedNodeCount,
            int createdEdgeCount,
            int reusedEdgeCount,
            List<GraphMaterialResponses.IssueData> issues) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchPreviewData(List<PreviewData> materials) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchItemData(
            GraphMaterialResponses.ContentRefData contentRef,
            boolean success,
            PublicationData result,
            String failureCode,
            String failureMessage) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchData(List<BatchItemData> materials) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record WithdrawalPreviewData(
            GraphMaterialResponses.ContentRefData materialRef,
            int nodeMappingCount,
            int edgeMappingCount,
            List<GraphPublishedResponses.NodeData> governedNodes,
            List<GraphPublishedResponses.EdgeData> governedEdges) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchWithdrawalPreviewItemData(
            GraphMaterialResponses.ContentRefData contentRef,
            WithdrawalPreviewData preview,
            String failureCode,
            String failureMessage) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchWithdrawalPreviewData(List<BatchWithdrawalPreviewItemData> materials) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record WithdrawalResultData(
            GraphMaterialResponses.ContentRefData contentRef,
            boolean success,
            GraphMaterialResponses.MaterialData result,
            String failureCode,
            String failureMessage) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record BatchWithdrawalData(String batchId, List<WithdrawalResultData> materials) {}
}
