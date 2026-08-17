package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;

public record GraphPublishedAdjacencyResult(
        GraphPublishedNode subject, GraphPublishedEdge relation, GraphPublishedNode object) {}
