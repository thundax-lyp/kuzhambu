package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;

public record GraphPublishedNodeDeleteQuery(GraphPublishedNodeId nodeId, boolean cascadeEdges) {}
