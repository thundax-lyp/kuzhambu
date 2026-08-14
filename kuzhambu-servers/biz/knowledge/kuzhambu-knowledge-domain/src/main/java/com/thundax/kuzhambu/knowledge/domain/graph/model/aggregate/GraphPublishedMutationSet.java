package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphPublishedMutationSet {

    private final List<GraphPublishedNode> createdNodes = new ArrayList<>();
    private final List<GraphPublishedNode> updatedNodes = new ArrayList<>();
    private final List<GraphPublishedEdge> updatedEdges = new ArrayList<>();

    public void addCreatedNode(GraphPublishedNode node) {
        addIfAbsent(createdNodes, node);
    }

    public void addUpdatedNode(GraphPublishedNode node) {
        addIfAbsent(updatedNodes, node);
    }

    public void addUpdatedEdge(GraphPublishedEdge edge) {
        addIfAbsent(updatedEdges, edge);
    }

    public List<GraphPublishedNode> createdNodes() {
        return Collections.unmodifiableList(createdNodes);
    }

    public List<GraphPublishedNode> updatedNodes() {
        return Collections.unmodifiableList(updatedNodes);
    }

    public List<GraphPublishedEdge> updatedEdges() {
        return Collections.unmodifiableList(updatedEdges);
    }

    private <T> void addIfAbsent(List<T> items, T item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
        }
    }
}
