package com.thundax.kuzhambu.knowledge.domain.graph.model.operation;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphPublicationChangeSet {

    private final List<GraphPublishedNode> createdNodes = new ArrayList<>();
    private final List<GraphPublishedNode> reusedNodes = new ArrayList<>();
    private final List<GraphPublishedEdge> createdEdges = new ArrayList<>();
    private final List<GraphPublishedEdge> reusedEdges = new ArrayList<>();
    private final List<GraphPublishedNodeProperty> nodeProperties = new ArrayList<>();
    private final List<GraphPublishedEdgeProperty> edgeProperties = new ArrayList<>();
    private final List<GraphPublishedNodeMaterial> nodeMaterials = new ArrayList<>();
    private final List<GraphPublishedEdgeMaterial> edgeMaterials = new ArrayList<>();
    private final List<ValidationIssue> issues = new ArrayList<>();

    public void addCreatedNode(GraphPublishedNode node) {
        addIfAbsent(createdNodes, node);
    }

    public void addReusedNode(GraphPublishedNode node) {
        addIfAbsent(reusedNodes, node);
    }

    public void addCreatedEdge(GraphPublishedEdge edge) {
        addIfAbsent(createdEdges, edge);
    }

    public void addReusedEdge(GraphPublishedEdge edge) {
        addIfAbsent(reusedEdges, edge);
    }

    public void addNodeProperty(GraphPublishedNodeProperty property) {
        addIfAbsent(nodeProperties, property);
    }

    public void addEdgeProperty(GraphPublishedEdgeProperty property) {
        addIfAbsent(edgeProperties, property);
    }

    public void addNodeMaterial(GraphPublishedNodeMaterial material) {
        addIfAbsent(nodeMaterials, material);
    }

    public void addEdgeMaterial(GraphPublishedEdgeMaterial material) {
        addIfAbsent(edgeMaterials, material);
    }

    public void addIssue(ValidationIssue issue) {
        addIfAbsent(issues, issue);
    }

    public boolean hasBlockingIssue() {
        return issues.stream().anyMatch(ValidationIssue::blocking);
    }

    public List<GraphPublishedNode> createdNodes() {
        return Collections.unmodifiableList(createdNodes);
    }

    public List<GraphPublishedNode> reusedNodes() {
        return Collections.unmodifiableList(reusedNodes);
    }

    public List<GraphPublishedEdge> createdEdges() {
        return Collections.unmodifiableList(createdEdges);
    }

    public List<GraphPublishedEdge> reusedEdges() {
        return Collections.unmodifiableList(reusedEdges);
    }

    public List<GraphPublishedNodeProperty> nodeProperties() {
        return Collections.unmodifiableList(nodeProperties);
    }

    public List<GraphPublishedEdgeProperty> edgeProperties() {
        return Collections.unmodifiableList(edgeProperties);
    }

    public List<GraphPublishedNodeMaterial> nodeMaterials() {
        return Collections.unmodifiableList(nodeMaterials);
    }

    public List<GraphPublishedEdgeMaterial> edgeMaterials() {
        return Collections.unmodifiableList(edgeMaterials);
    }

    public List<ValidationIssue> issues() {
        return Collections.unmodifiableList(issues);
    }

    private <T> void addIfAbsent(List<T> items, T item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
        }
    }

    public record ValidationIssue(String code, String objectType, String objectKey, boolean blocking, String message) {}
}
