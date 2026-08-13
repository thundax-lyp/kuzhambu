package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.List;

public record GraphQualitySnapshot(
        long isolatedNodeCount,
        long missingCoreRelationNodeCount,
        List<GraphPublishedNode> isolatedNodes,
        List<GraphPublishedNode> missingCoreRelationNodes) {}
