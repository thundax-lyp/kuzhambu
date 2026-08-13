package com.thundax.kuzhambu.knowledge.domain.graph.model.operation;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class GraphPublication {

    private static final String ISSUE_PUBLISHED_OBJECT_DELETED = "PUBLISHED_OBJECT_DELETED";

    private final GraphPublicationContext context;
    private final GraphPublicationChangeSet changes;

    private GraphPublication(GraphPublicationContext context, GraphPublicationChangeSet changes) {
        this.context = context;
        this.changes = changes;
    }

    public static GraphPublication plan(GraphPublicationContext context) {
        validateContext(context);
        GraphPublicationChangeSet changes = new GraphPublicationChangeSet();
        Map<GraphMaterialNodeId, GraphPublishedNode> publishedNodesByMaterialNodeId = new HashMap<>();
        for (GraphMaterialNode materialNode : context.materialGraph().nodes()) {
            GraphPublishedNode publishedNode = context.matchedNodesByMaterialNodeId() == null
                    ? null
                    : context.matchedNodesByMaterialNodeId().get(materialNode.getId());
            if (publishedNode == null) {
                publishedNode = toCreatedNode(materialNode, context.modifiedAt());
                changes.addCreatedNode(publishedNode);
            } else {
                addDeletedObjectIssueIfNeeded(
                        changes, "NODE", materialNode.getNodeKey().value(), publishedNode);
                changes.addReusedNode(publishedNode);
            }
            publishedNodesByMaterialNodeId.put(materialNode.getId(), publishedNode);
            changes.addNodeMaterial(new GraphPublishedNodeMaterial(
                    publishedNode.getId(), context.materialGraph().material().getContentRef(), null));
        }
        for (GraphMaterialEdge materialEdge : context.materialGraph().edges()) {
            GraphPublishedEdge publishedEdge = context.matchedEdgesByMaterialEdgeId() == null
                    ? null
                    : context.matchedEdgesByMaterialEdgeId().get(materialEdge.getId());
            if (publishedEdge == null) {
                publishedEdge = toCreatedEdge(materialEdge, publishedNodesByMaterialNodeId, context.modifiedAt());
                changes.addCreatedEdge(publishedEdge);
            } else {
                addDeletedObjectIssueIfNeeded(
                        changes, "EDGE", materialEdge.getEdgeKey().value(), publishedEdge);
                changes.addReusedEdge(publishedEdge);
            }
            changes.addEdgeMaterial(new GraphPublishedEdgeMaterial(
                    publishedEdge.getId(), context.materialGraph().material().getContentRef(), null));
        }
        return new GraphPublication(context, changes);
    }

    public void validateForPublication() {
        context.materialGraph().material().requireReady();
        if (changes.hasBlockingIssue()) {
            throw new DomainException("Graph publication has blocking issues");
        }
    }

    public GraphPublicationChangeSet changes() {
        return changes;
    }

    public GraphPublicationContext context() {
        return context;
    }

    public int createdNodeCount() {
        return changes.createdNodes().size();
    }

    public int reusedNodeCount() {
        return changes.reusedNodes().size();
    }

    public int createdEdgeCount() {
        return changes.createdEdges().size();
    }

    public int reusedEdgeCount() {
        return changes.reusedEdges().size();
    }

    private static void validateContext(GraphPublicationContext context) {
        if (context == null || context.materialGraph() == null) {
            throw new DomainException("Graph publication context is required");
        }
        context.materialGraph().validate();
    }

    private static GraphPublishedNode toCreatedNode(GraphMaterialNode materialNode, Instant modifiedAt) {
        GraphPublishedNode node = new GraphPublishedNode();
        node.setNodeKey(materialNode.getNodeKey());
        node.setNodeType(materialNode.getNodeType());
        node.setName(materialNode.getName());
        node.setSource(materialNode.getSource());
        node.setStatus(GraphPublishedStatus.ACTIVE);
        node.setModifiedAt(modifiedAt);
        return node;
    }

    private static GraphPublishedEdge toCreatedEdge(
            GraphMaterialEdge materialEdge,
            Map<GraphMaterialNodeId, GraphPublishedNode> publishedNodesByMaterialNodeId,
            Instant modifiedAt) {
        GraphPublishedNode sourceNode = publishedNodesByMaterialNodeId.get(materialEdge.getSourceNodeId());
        GraphPublishedNode targetNode = publishedNodesByMaterialNodeId.get(materialEdge.getTargetNodeId());
        GraphPublishedEdge edge = new GraphPublishedEdge();
        edge.setEdgeKey(materialEdge.getEdgeKey());
        edge.setSourceNodeId(sourceNode == null ? null : sourceNode.getId());
        edge.setTargetNodeId(targetNode == null ? null : targetNode.getId());
        edge.setRelationType(materialEdge.getRelationType());
        edge.setSource(materialEdge.getSource());
        edge.setQualifiersJson(materialEdge.getQualifiersJson());
        edge.setStatus(GraphPublishedStatus.ACTIVE);
        edge.setModifiedAt(modifiedAt);
        return edge;
    }

    private static void addDeletedObjectIssueIfNeeded(
            GraphPublicationChangeSet changes, String objectType, String objectKey, GraphPublishedNode node) {
        if (node.getStatus() == GraphPublishedStatus.DELETED) {
            changes.addIssue(new GraphPublicationChangeSet.ValidationIssue(
                    ISSUE_PUBLISHED_OBJECT_DELETED, objectType, objectKey, true, "Published graph object is deleted"));
        }
    }

    private static void addDeletedObjectIssueIfNeeded(
            GraphPublicationChangeSet changes, String objectType, String objectKey, GraphPublishedEdge edge) {
        if (edge.getStatus() == GraphPublishedStatus.DELETED) {
            changes.addIssue(new GraphPublicationChangeSet.ValidationIssue(
                    ISSUE_PUBLISHED_OBJECT_DELETED, objectType, objectKey, true, "Published graph object is deleted"));
        }
    }
}
