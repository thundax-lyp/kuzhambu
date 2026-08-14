package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.List;

public record GraphPublishedEdgeDetailResult(
        GraphPublishedEdge edge,
        GraphPublishedNode sourceNode,
        GraphPublishedNode targetNode,
        List<GraphPublishedEdgeProperty> properties,
        List<GraphPublishedEdgeMaterial> materials) {}
