package com.thundax.kuzhambu.knowledge.domain.graph.model.aggregate;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.util.List;

public record GraphPublishedNodeSplitSpec(
        GraphPublishedNodeId sourceNodeId,
        GraphPublishedNode splitNode,
        List<GraphPublishedEdgeId> reassignedEdgeIds) {}
