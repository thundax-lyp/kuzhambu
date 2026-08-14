package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialNodeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphMaterialGraph {

    private final GraphMaterial material;
    private final List<GraphMaterialNode> nodes;
    private final List<GraphMaterialEdge> edges;

    private GraphMaterialGraph(GraphMaterial material, List<GraphMaterialNode> nodes, List<GraphMaterialEdge> edges) {
        this.material = material;
        this.nodes = new ArrayList<>(nodes == null ? List.of() : nodes);
        this.edges = new ArrayList<>(edges == null ? List.of() : edges);
    }

    public static GraphMaterialGraph of(
            GraphMaterial material, List<GraphMaterialNode> nodes, List<GraphMaterialEdge> edges) {
        GraphMaterialGraph graph = new GraphMaterialGraph(material, nodes, edges);
        graph.validate();
        return graph;
    }

    public GraphMaterialNode addNode(GraphMaterialNode node) {
        material.requireEditable();
        requireNode(node);
        rejectDuplicateNodeKey(node, null);
        nodes.add(node);
        material.refreshStatus(false);
        return node;
    }

    public void updateNode(GraphMaterialNode node) {
        material.requireEditable();
        requireNode(node);
        int index = indexOfNode(node.getId());
        rejectDuplicateNodeKey(node, node.getId());
        nodes.set(index, node);
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
    }

    public void removeNode(GraphMaterialNodeId nodeId) {
        material.requireEditable();
        GraphMaterialNode node = requireNodeById(nodeId);
        removeNode(node);
        material.refreshStatus(nodes.isEmpty());
    }

    public GraphMaterialEdge addEdge(GraphMaterialEdge edge) {
        material.requireEditable();
        requireEdge(edge);
        rejectDuplicateEdgeKey(edge, null);
        edges.add(edge);
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
        return edge;
    }

    public void updateEdge(GraphMaterialEdge edge) {
        material.requireEditable();
        requireEdge(edge);
        int index = indexOfEdge(edge.getId());
        rejectDuplicateEdgeKey(edge, edge.getId());
        edges.set(index, edge);
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
    }

    public void removeEdge(GraphMaterialEdgeId edgeId) {
        material.requireEditable();
        GraphMaterialEdge edge = requireEdgeById(edgeId);
        edges.remove(edge);
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
    }

    public void mergeNodes(GraphMaterialNodeId retainedNodeId, List<GraphMaterialNodeId> mergedNodeIds) {
        material.requireEditable();
        GraphMaterialNode retainedNode = requireNodeById(retainedNodeId);
        for (GraphMaterialNodeId mergedNodeId :
                mergedNodeIds == null ? List.<GraphMaterialNodeId>of() : mergedNodeIds) {
            if (retainedNodeId.equals(mergedNodeId)) {
                continue;
            }
            GraphMaterialNode mergedNode = requireNodeById(mergedNodeId);
            reassignEdges(mergedNode.getId(), retainedNode);
            removeNode(mergedNode);
        }
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
    }

    public void splitNode(
            GraphMaterialNodeId sourceNodeId,
            GraphMaterialNode splitNode,
            List<GraphMaterialEdgeId> reassignedEdgeIds) {
        material.requireEditable();
        GraphMaterialNode sourceNode = requireNodeById(sourceNodeId);
        requireNode(splitNode);
        rejectDuplicateNodeKey(splitNode, splitNode.getId());
        nodes.add(splitNode);
        for (GraphMaterialEdgeId edgeId :
                reassignedEdgeIds == null ? List.<GraphMaterialEdgeId>of() : reassignedEdgeIds) {
            GraphMaterialEdge edge = requireEdgeById(edgeId);
            if (!edge.connects(sourceNode.getId())) {
                throw new DomainException("Graph material edge does not connect source node");
            }
            GraphMaterialNode targetNode = endpoint(edge.getSourceNodeId()).equals(sourceNode)
                    ? endpoint(edge.getTargetNodeId())
                    : endpoint(edge.getSourceNodeId());
            edge.replaceEndpoint(splitNode.getId(), targetNode.getId());
        }
        material.refreshStatus(nodes.isEmpty() && edges.isEmpty());
    }

    public void validate() {
        if (material == null || material.getContentRef() == null) {
            throw new DomainException("Graph material is required");
        }
        ContentRef materialRef = material.getContentRef();
        Set<GraphNodeKey> nodeKeys = new HashSet<>();
        Set<GraphMaterialNodeId> nodeIds = new HashSet<>();
        for (GraphMaterialNode node : nodes) {
            requireNode(node);
            if (node.getId() != null && !nodeIds.add(node.getId())) {
                throw new DomainException("Graph material node id is duplicated");
            }
            if (node.getNodeKey() != null && !nodeKeys.add(node.getNodeKey())) {
                throw new DomainException("Graph material node key is duplicated");
            }
            node.requireMaterial(materialRef);
        }
        Set<GraphEdgeKey> edgeKeys = new HashSet<>();
        for (GraphMaterialEdge edge : edges) {
            requireEdge(edge);
            if (!nodeIds.contains(edge.getSourceNodeId()) || !nodeIds.contains(edge.getTargetNodeId())) {
                throw new DomainException("Graph material edge endpoint node is missing");
            }
            if (edge.getEdgeKey() != null && !edgeKeys.add(edge.getEdgeKey())) {
                throw new DomainException("Graph material edge key is duplicated");
            }
        }
    }

    public GraphMaterial material() {
        return material;
    }

    public List<GraphMaterialNode> nodes() {
        return List.copyOf(nodes);
    }

    public List<GraphMaterialEdge> edges() {
        return List.copyOf(edges);
    }

    private void requireNode(GraphMaterialNode node) {
        if (node == null) {
            throw new DomainException("Graph material node is required");
        }
        node.requireMaterial(material.getContentRef());
        node.validateRequiredFields();
    }

    private void requireEdge(GraphMaterialEdge edge) {
        if (edge == null) {
            throw new DomainException("Graph material edge is required");
        }
        edge.requireMaterial(material.getContentRef());
        edge.validateRequiredFields();
        endpoint(edge.getSourceNodeId());
        endpoint(edge.getTargetNodeId());
    }

    private GraphMaterialNode requireNodeById(GraphMaterialNodeId nodeId) {
        if (nodeId == null) {
            throw new DomainException("Graph material node id is required");
        }
        return endpoint(nodeId);
    }

    private GraphMaterialEdge requireEdgeById(GraphMaterialEdgeId edgeId) {
        if (edgeId == null) {
            throw new DomainException("Graph material edge id is required");
        }
        return edges.stream()
                .filter(edge -> edgeId.equals(edge.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Graph material edge not found"));
    }

    private GraphMaterialNode endpoint(GraphMaterialNodeId nodeId) {
        return nodes.stream()
                .filter(node -> nodeId != null && nodeId.equals(node.getId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Graph material node not found"));
    }

    private int indexOfNode(GraphMaterialNodeId nodeId) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodeId != null && nodeId.equals(nodes.get(i).getId())) {
                return i;
            }
        }
        throw new DomainException("Graph material node not found");
    }

    private int indexOfEdge(GraphMaterialEdgeId edgeId) {
        for (int i = 0; i < edges.size(); i++) {
            if (edgeId != null && edgeId.equals(edges.get(i).getId())) {
                return i;
            }
        }
        throw new DomainException("Graph material edge not found");
    }

    private void rejectDuplicateNodeKey(GraphMaterialNode node, GraphMaterialNodeId excludedId) {
        for (GraphMaterialNode existing : nodes) {
            if (existing.getNodeKey() != null
                    && existing.getNodeKey().equals(node.getNodeKey())
                    && (excludedId == null || !excludedId.equals(existing.getId()))) {
                throw new DomainException("Graph material node key is duplicated");
            }
        }
    }

    private void rejectDuplicateEdgeKey(GraphMaterialEdge edge, GraphMaterialEdgeId excludedId) {
        for (GraphMaterialEdge existing : edges) {
            if (existing.getEdgeKey() != null
                    && existing.getEdgeKey().equals(edge.getEdgeKey())
                    && (excludedId == null || !excludedId.equals(existing.getId()))) {
                throw new DomainException("Graph material edge key is duplicated");
            }
        }
    }

    private void reassignEdges(GraphMaterialNodeId mergedNodeId, GraphMaterialNode retainedNode) {
        for (GraphMaterialEdge edge : edges) {
            if (!edge.connects(mergedNodeId)) {
                continue;
            }
            GraphMaterialNode source = endpoint(edge.getSourceNodeId());
            GraphMaterialNode target = endpoint(edge.getTargetNodeId());
            if (mergedNodeId.equals(edge.getSourceNodeId())) {
                source = retainedNode;
            }
            if (mergedNodeId.equals(edge.getTargetNodeId())) {
                target = retainedNode;
            }
            edge.replaceEndpoint(source.getId(), target.getId());
        }
    }

    private void removeNode(GraphMaterialNode node) {
        nodes.remove(node);
        for (GraphMaterialEdge edge : new ArrayList<>(edges)) {
            if (edge.connects(node.getId())) {
                edges.remove(edge);
            }
        }
    }

    public void deduplicateEdges() {
        Set<GraphEdgeKey> seenKeys = new HashSet<>();
        for (GraphMaterialEdge edge : new ArrayList<>(edges)) {
            GraphEdgeKey key = edge.getEdgeKey();
            if (key != null && !seenKeys.add(key)) {
                edges.remove(edge);
            }
        }
    }
}
