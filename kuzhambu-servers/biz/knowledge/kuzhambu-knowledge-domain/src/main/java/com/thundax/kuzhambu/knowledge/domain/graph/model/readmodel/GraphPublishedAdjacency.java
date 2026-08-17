package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;

public record GraphPublishedAdjacency(
        GraphPublishedNode subject, GraphPublishedEdge relation, GraphPublishedNode object) {}
