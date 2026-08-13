package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GraphDocumentPlan {

    private final Map<String, GraphMaterialNode> matchedNodesByDocumentId = new LinkedHashMap<>();
    private final Map<String, GraphMaterialNode> createdNodesByDocumentId = new LinkedHashMap<>();
    private final Map<String, GraphMaterialNode> updatedNodesByDocumentId = new LinkedHashMap<>();
    private final List<GraphMaterialNode> deletedNodes = new ArrayList<>();
    private final List<EdgeSpec> createdEdges = new ArrayList<>();
    private final List<EdgeSpec> updatedEdges = new ArrayList<>();
    private final List<GraphMaterialEdge> deletedEdges = new ArrayList<>();

    public void addCreatedNode(String documentNodeId, GraphMaterialNode node) {
        createdNodesByDocumentId.put(documentNodeId, node);
    }

    public void addUpdatedNode(String documentNodeId, GraphMaterialNode existingNode, GraphMaterialNode node) {
        matchedNodesByDocumentId.put(documentNodeId, existingNode);
        updatedNodesByDocumentId.put(documentNodeId, node);
    }

    public void addUnchangedNode(String documentNodeId, GraphMaterialNode existingNode) {
        matchedNodesByDocumentId.put(documentNodeId, existingNode);
    }

    public void addDeletedNode(GraphMaterialNode node) {
        deletedNodes.add(node);
    }

    public void addCreatedEdge(EdgeSpec edge) {
        createdEdges.add(edge);
    }

    public void addUpdatedEdge(EdgeSpec edge) {
        updatedEdges.add(edge);
    }

    public void addDeletedEdge(GraphMaterialEdge edge) {
        deletedEdges.add(edge);
    }

    public Map<String, GraphMaterialNode> matchedNodesByDocumentId() {
        return Collections.unmodifiableMap(matchedNodesByDocumentId);
    }

    public Map<String, GraphMaterialNode> createdNodesByDocumentId() {
        return Collections.unmodifiableMap(createdNodesByDocumentId);
    }

    public Map<String, GraphMaterialNode> updatedNodesByDocumentId() {
        return Collections.unmodifiableMap(updatedNodesByDocumentId);
    }

    public List<GraphMaterialNode> createdNodes() {
        return List.copyOf(createdNodesByDocumentId.values());
    }

    public List<GraphMaterialNode> updatedNodes() {
        return List.copyOf(updatedNodesByDocumentId.values());
    }

    public List<GraphMaterialNode> deletedNodes() {
        return Collections.unmodifiableList(deletedNodes);
    }

    public List<EdgeSpec> createdEdges() {
        return Collections.unmodifiableList(createdEdges);
    }

    public List<EdgeSpec> updatedEdges() {
        return Collections.unmodifiableList(updatedEdges);
    }

    public List<GraphMaterialEdge> deletedEdges() {
        return Collections.unmodifiableList(deletedEdges);
    }

    public Map<String, GraphMaterialNode> materialNodesByDocumentId() {
        Map<String, GraphMaterialNode> result = new LinkedHashMap<>(matchedNodesByDocumentId);
        result.putAll(updatedNodesByDocumentId);
        result.putAll(createdNodesByDocumentId);
        return Collections.unmodifiableMap(result);
    }

    public record EdgeSpec(
            String documentEdgeId,
            GraphMaterialEdge existingEdge,
            String sourceDocumentNodeId,
            String targetDocumentNodeId,
            GraphEdgeKey edgeKey,
            String relationType,
            GraphSourceType source,
            String qualifiersJson) {}
}
