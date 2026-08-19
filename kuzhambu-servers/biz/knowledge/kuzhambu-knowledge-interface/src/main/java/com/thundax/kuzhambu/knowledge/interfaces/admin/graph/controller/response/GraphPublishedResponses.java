package com.thundax.kuzhambu.knowledge.interfaces.admin.graph.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/** 图谱发布空间 HTTP 响应契约。 */
public final class GraphPublishedResponses {
    private GraphPublishedResponses() {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record NodeData(String id, String nodeType, String name, String source, String status, String lockVersion) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record EdgeData(
            String id,
            String sourceNodeId,
            String targetNodeId,
            String relationType,
            Map<String, Object> qualifiers,
            String source,
            String status,
            String lockVersion,
            String sourceNodeName,
            String targetNodeName) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record AdjacencyData(NodeData subject, EdgeData relation, NodeData object, boolean isolated) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SourceRefData(GraphMaterialResponses.ContentRefData contentRef, String auditLogId) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PropertyData(
            String id,
            String propertyName,
            String value,
            boolean preferred,
            String sourceType,
            SourceRefData sourceRef) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record MappingData(
            String id,
            String mappingType,
            String status,
            GraphMaterialResponses.ContentRefData contentRef,
            String publishedObjectId,
            Map<String, Object> sourceSnapshot) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record OperationData(
            String id,
            String operationType,
            String targetType,
            String targetId,
            String reason,
            String auditLogId,
            String operatorId,
            String operatorName,
            String occurredAt,
            String beforeSummary,
            String afterSummary) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record GovernanceImpactData(
            String impactToken,
            List<NodeData> nodes,
            List<EdgeData> edges,
            List<MappingData> nodeMappings,
            List<MappingData> edgeMappings,
            List<GraphMaterialResponses.IssueData> issues,
            boolean executable) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record NodeDetailData(
            NodeData node,
            List<PropertyData> properties,
            List<MappingData> materials,
            List<EdgeData> incidentEdges,
            List<OperationData> operations) {}

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record EdgeDetailData(
            EdgeData edge,
            NodeData sourceNode,
            NodeData targetNode,
            List<PropertyData> properties,
            List<MappingData> materials,
            List<OperationData> operations) {}
}
