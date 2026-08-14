package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import java.util.List;

public record GraphWorkbenchMetrics(
        long publishedNodeCount,
        long publishedEdgeCount,
        long coveredMaterialCount,
        long isolatedNodeCount,
        long missingCoreRelationNodeCount,
        List<GraphWorkbenchActivity> recentActivities,
        long pendingConflictCount) {}
