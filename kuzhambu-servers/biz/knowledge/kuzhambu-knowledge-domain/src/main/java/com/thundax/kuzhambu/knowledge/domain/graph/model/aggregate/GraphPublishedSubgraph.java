package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GraphPublishedSubgraph {

    private final List<GraphPublishedNode> nodes;
    private final List<GraphPublishedEdge> edges;

    public GraphPublishedSubgraph(List<GraphPublishedNode> nodes, List<GraphPublishedEdge> edges) {
        this.nodes = new ArrayList<>(nodes == null ? List.of() : nodes);
        this.edges = new ArrayList<>(edges == null ? List.of() : edges);
    }

    public GraphPublishedChangeSet deleteNode(GraphPublishedNodeId nodeId, boolean cascadeEdges, Instant modifiedAt) {
        GraphPublishedNode node = requireNode(nodeId);
        GraphPublishedChangeSet changes = new GraphPublishedChangeSet();
        List<GraphPublishedEdge> incidentEdges = incidentEdges(nodeId);
        if (!cascadeEdges && hasActiveEdge(incidentEdges)) {
            throw new DomainException("Graph published node has active incident edges");
        }
        for (GraphPublishedEdge edge : incidentEdges) {
            if (edge.getStatus() == GraphPublishedStatus.ACTIVE) {
                edge.delete(modifiedAt);
                changes.addUpdatedEdge(edge);
            }
        }
        node.delete(modifiedAt);
        changes.addUpdatedNode(node);
        return changes;
    }

    public GraphPublishedChangeSet deleteEdge(GraphPublishedEdgeId edgeId, Instant modifiedAt) {
        GraphPublishedEdge edge = requireEdge(edgeId);
        GraphPublishedChangeSet changes = new GraphPublishedChangeSet();
        edge.delete(modifiedAt);
        changes.addUpdatedEdge(edge);
        return changes;
    }

    public GraphPublishedChangeSet mergeNodes(
            GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds, Instant modifiedAt) {
        GraphPublishedNode retainedNode = requireNode(retainedNodeId);
        GraphPublishedChangeSet changes = new GraphPublishedChangeSet();
        for (GraphPublishedNodeId mergedNodeId :
                mergedNodeIds == null ? List.<GraphPublishedNodeId>of() : mergedNodeIds) {
            if (retainedNodeId.equals(mergedNodeId)) {
                continue;
            }
            GraphPublishedNode mergedNode = requireNode(mergedNodeId);
            for (GraphPublishedEdge edge : incidentEdges(mergedNodeId)) {
                if (mergedNodeId.equals(edge.getSourceNodeId())) {
                    edge.setSourceNodeId(retainedNode.getId());
                }
                if (mergedNodeId.equals(edge.getTargetNodeId())) {
                    edge.setTargetNodeId(retainedNode.getId());
                }
                edge.touch(modifiedAt);
                changes.addUpdatedEdge(edge);
            }
            mergedNode.delete(modifiedAt);
            changes.addUpdatedNode(mergedNode);
        }
        retainedNode.touch(modifiedAt);
        changes.addUpdatedNode(retainedNode);
        return changes;
    }

    public GraphPublishedChangeSet splitNode(GraphPublishedNodeSplitSpec spec, Instant modifiedAt) {
        if (spec == null || spec.splitNode() == null) {
            throw new DomainException("Graph published node split spec is required");
        }
        GraphPublishedNode sourceNode = requireNode(spec.sourceNodeId());
        GraphPublishedNode splitNode = spec.splitNode();
        splitNode.validateRequiredFields();
        splitNode.touch(modifiedAt);
        nodes.add(splitNode);
        GraphPublishedChangeSet changes = new GraphPublishedChangeSet();
        changes.addCreatedNode(splitNode);
        for (GraphPublishedEdgeId edgeId :
                spec.reassignedEdgeIds() == null ? List.<GraphPublishedEdgeId>of() : spec.reassignedEdgeIds()) {
            GraphPublishedEdge edge = requireEdge(edgeId);
            if (!connects(edge, sourceNode.getId())) {
                throw new DomainException("Graph published edge does not connect source node");
            }
            if (sourceNode.getId().equals(edge.getSourceNodeId())) {
                edge.setSourceNodeId(splitNode.getId());
            } else {
                edge.setTargetNodeId(splitNode.getId());
            }
            edge.touch(modifiedAt);
            changes.addUpdatedEdge(edge);
        }
        sourceNode.touch(modifiedAt);
        changes.addUpdatedNode(sourceNode);
        return changes;
    }

    private GraphPublishedNode requireNode(GraphPublishedNodeId nodeId) {
        return nodes.stream()
                .filter(node -> nodeId != null && nodeId.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Graph published node not found"));
    }

    private GraphPublishedEdge requireEdge(GraphPublishedEdgeId edgeId) {
        return edges.stream()
                .filter(edge -> edgeId != null && edgeId.equals(edge.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Graph published edge not found"));
    }

    private List<GraphPublishedEdge> incidentEdges(GraphPublishedNodeId nodeId) {
        return edges.stream().filter(edge -> connects(edge, nodeId)).toList();
    }

    private boolean hasActiveEdge(List<GraphPublishedEdge> edges) {
        return edges.stream().anyMatch(edge -> edge.getStatus() == GraphPublishedStatus.ACTIVE);
    }

    private boolean connects(GraphPublishedEdge edge, GraphPublishedNodeId nodeId) {
        return edge != null
                && nodeId != null
                && (nodeId.equals(edge.getSourceNodeId()) || nodeId.equals(edge.getTargetNodeId()));
    }
}
