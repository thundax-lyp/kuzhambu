package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphEdgePublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphExtractionResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphIncidentEdgesResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphMaterialResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphNodePublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationPreviewResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublicationResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedEdgeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphPublishedNodeDetailResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphSearchResult;
import com.thundax.kuzhambu.knowledge.application.graph.result.GraphValidationIssueResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate.GraphMaterialGraph;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublication;
import com.thundax.kuzhambu.knowledge.domain.graph.model.operation.GraphPublicationChangeSet;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphPublishedSearchHit;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeSlice;
import java.util.List;
import java.util.Map;

public final class GraphApplicationAssembler {

    private static final String SEVERITY_BLOCKING = "BLOCKING";
    private static final String SEVERITY_WARNING = "WARNING";

    private GraphApplicationAssembler() {}

    public static GraphMaterialResult toMaterialResult(GraphMaterialGraph graph) {
        return graph == null ? null : new GraphMaterialResult(graph.material(), graph.nodes(), graph.edges());
    }

    public static GraphPublishedNodeDetailResult toNodeDetail(
            GraphPublishedNode node,
            List<GraphPublishedNodeProperty> properties,
            List<GraphPublishedNodeMaterial> materials,
            List<GraphPublishedEdge> incidentEdges) {
        return new GraphPublishedNodeDetailResult(
                node, safeList(properties), safeList(materials), safeList(incidentEdges));
    }

    public static GraphPublishedEdgeDetailResult toEdgeDetail(
            GraphPublishedEdge edge,
            GraphPublishedNode sourceNode,
            GraphPublishedNode targetNode,
            List<GraphPublishedEdgeProperty> properties,
            List<GraphPublishedEdgeMaterial> materials) {
        return new GraphPublishedEdgeDetailResult(
                edge, sourceNode, targetNode, safeList(properties), safeList(materials));
    }

    public static GraphPublicationPreviewResult toPublicationPreview(GraphPublication publication) {
        if (publication == null) {
            return null;
        }
        GraphMaterialGraph graph = publication.context().materialGraph();
        GraphPublicationChangeSet changes = publication.changes();
        List<GraphValidationIssueResult> issues = toIssues(changes.issues());
        Map<GraphMaterialNodeId, GraphPublishedNode> matchedNodes =
                safeMap(publication.context().matchedNodesByMaterialNodeId());
        Map<GraphMaterialEdgeId, GraphPublishedEdge> matchedEdges =
                safeMap(publication.context().matchedEdgesByMaterialEdgeId());
        return new GraphPublicationPreviewResult(
                graph.material().getContentRef(),
                graph.material().getLockVersion(),
                graph.nodes().stream()
                        .map(node -> new GraphNodePublicationPreviewResult(
                                node,
                                matchedNodes.get(node.getId()),
                                issuesFor(
                                        issues,
                                        "NODE",
                                        node.getNodeKey() == null
                                                ? null
                                                : node.getNodeKey().value())))
                        .toList(),
                graph.edges().stream()
                        .map(edge -> new GraphEdgePublicationPreviewResult(
                                edge,
                                matchedEdges.get(edge.getId()),
                                issuesFor(
                                        issues,
                                        "EDGE",
                                        edge.getEdgeKey() == null
                                                ? null
                                                : edge.getEdgeKey().value())))
                        .toList(),
                issues,
                !changes.hasBlockingIssue());
    }

    public static GraphPublicationResult toPublicationResult(
            ContentRef materialRef, GraphMaterialStatus materialStatus, GraphPublication publication) {
        GraphPublicationChangeSet changes = publication == null ? null : publication.changes();
        List<GraphValidationIssueResult> issues = changes == null ? List.of() : toIssues(changes.issues());
        boolean success = changes != null && !changes.hasBlockingIssue();
        return new GraphPublicationResult(
                materialRef,
                materialStatus,
                success,
                success ? null : "Graph publication has blocking issues",
                publication == null ? 0 : publication.createdNodeCount(),
                publication == null ? 0 : publication.reusedNodeCount(),
                publication == null ? 0 : publication.createdEdgeCount(),
                publication == null ? 0 : publication.reusedEdgeCount(),
                issues);
    }

    public static GraphExtractionResult toExtractionResult(
            AiBatchJobFacadeResponse response, AiCandidateFacadeDto candidate) {
        if (response == null) {
            return null;
        }
        return new GraphExtractionResult(
                ContentRefCodec.toDomain(response.getContentType(), response.getContentId()),
                response.getBatchId(),
                candidate == null ? null : candidate.getCandidateId(),
                response.getStatus(),
                response.getTotalCount(),
                response.getSuccessCount(),
                response.getFailedCount(),
                null,
                response.getFailureSummaryJson(),
                response.getFailureSummaryJson(),
                response.getRequestedAt(),
                response.getCompletedAt());
    }

    public static GraphSearchResult toSearchResult(GraphPublishedSearchHit hit) {
        return hit == null ? null : new GraphSearchResult(hit.objectType(), hit.node(), hit.edge());
    }

    public static GraphIncidentEdgesResult toIncidentEdgesResult(
            List<GraphPublishedNode> nodes, GraphPublishedEdgeSlice edgeSlice) {
        return new GraphIncidentEdgesResult(
                safeList(nodes),
                edgeSlice == null ? List.of() : safeList(edgeSlice.edges()),
                edgeSlice == null ? null : edgeSlice.nextCursor(),
                edgeSlice != null && edgeSlice.truncated());
    }

    private static List<GraphValidationIssueResult> toIssues(List<GraphPublicationChangeSet.ValidationIssue> issues) {
        return safeList(issues).stream()
                .map(issue -> new GraphValidationIssueResult(
                        issue.code(),
                        issue.blocking() ? SEVERITY_BLOCKING : SEVERITY_WARNING,
                        issue.objectType(),
                        issue.objectKey(),
                        null,
                        issue.message()))
                .toList();
    }

    private static List<GraphValidationIssueResult> issuesFor(
            List<GraphValidationIssueResult> issues, String objectType, String objectId) {
        return safeList(issues).stream()
                .filter(issue ->
                        objectType.equals(issue.objectType()) && java.util.Objects.equals(objectId, issue.objectId()))
                .toList();
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private static <K, V> Map<K, V> safeMap(Map<K, V> values) {
        return values == null ? Map.of() : Map.copyOf(values);
    }
}
