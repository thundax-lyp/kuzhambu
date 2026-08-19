package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public record GraphOneHopEdgesQuery(List<GraphPublishedNodeId> nodeIds, GraphPublishedEdgeId afterEdgeId) {}
