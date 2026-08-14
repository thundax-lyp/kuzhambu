package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import java.util.List;

public record GraphQualityResult(
        long isolatedNodeCount,
        long missingCoreRelationNodeCount,
        List<GraphPublishedNode> isolatedNodes,
        List<GraphPublishedNode> missingCoreRelationNodes) {}
