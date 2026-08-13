package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphMaterialChangeSet {

    private final List<GraphMaterialNode> createdNodes = new ArrayList<>();
    private final List<GraphMaterialNode> updatedNodes = new ArrayList<>();
    private final List<GraphMaterialNode> deletedNodes = new ArrayList<>();
    private final List<GraphMaterialEdge> createdEdges = new ArrayList<>();
    private final List<GraphMaterialEdge> updatedEdges = new ArrayList<>();
    private final List<GraphMaterialEdge> deletedEdges = new ArrayList<>();

    public static GraphMaterialChangeSet empty() {
        return new GraphMaterialChangeSet();
    }

    public void addCreatedNode(GraphMaterialNode node) {
        addIfAbsent(createdNodes, node);
    }

    public void addUpdatedNode(GraphMaterialNode node) {
        addIfAbsent(updatedNodes, node);
    }

    public void addDeletedNode(GraphMaterialNode node) {
        addIfAbsent(deletedNodes, node);
    }

    public void addCreatedEdge(GraphMaterialEdge edge) {
        addIfAbsent(createdEdges, edge);
    }

    public void addUpdatedEdge(GraphMaterialEdge edge) {
        addIfAbsent(updatedEdges, edge);
    }

    public void addDeletedEdge(GraphMaterialEdge edge) {
        addIfAbsent(deletedEdges, edge);
    }

    public List<GraphMaterialNode> createdNodes() {
        return Collections.unmodifiableList(createdNodes);
    }

    public List<GraphMaterialNode> updatedNodes() {
        return Collections.unmodifiableList(updatedNodes);
    }

    public List<GraphMaterialNode> deletedNodes() {
        return Collections.unmodifiableList(deletedNodes);
    }

    public List<GraphMaterialEdge> createdEdges() {
        return Collections.unmodifiableList(createdEdges);
    }

    public List<GraphMaterialEdge> updatedEdges() {
        return Collections.unmodifiableList(updatedEdges);
    }

    public List<GraphMaterialEdge> deletedEdges() {
        return Collections.unmodifiableList(deletedEdges);
    }

    private <T> void addIfAbsent(List<T> items, T item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
        }
    }
}
