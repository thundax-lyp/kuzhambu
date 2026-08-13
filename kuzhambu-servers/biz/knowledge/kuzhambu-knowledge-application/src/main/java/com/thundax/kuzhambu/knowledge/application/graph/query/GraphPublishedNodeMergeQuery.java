package com.thundax.kuzhambu.knowledge.application.graph.query;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public record GraphPublishedNodeMergeQuery(
        GraphPublishedNodeId retainedNodeId, List<GraphPublishedNodeId> mergedNodeIds) {}
