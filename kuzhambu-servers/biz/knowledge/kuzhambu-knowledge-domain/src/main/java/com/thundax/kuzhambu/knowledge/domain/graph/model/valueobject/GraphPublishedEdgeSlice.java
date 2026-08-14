package com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import java.util.List;

public record GraphPublishedEdgeSlice(
        List<GraphPublishedEdge> edges, GraphPublishedEdgeId nextCursor, boolean truncated) {}
