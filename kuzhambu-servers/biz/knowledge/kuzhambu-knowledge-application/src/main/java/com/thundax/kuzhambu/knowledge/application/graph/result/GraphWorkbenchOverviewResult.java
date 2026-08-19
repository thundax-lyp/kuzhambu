package com.thundax.kuzhambu.knowledge.application.graph.result;

import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchActivity;
import java.time.Instant;
import java.util.List;

public record GraphWorkbenchOverviewResult(
        Instant snapshotAt,
        long publishedNodeCount,
        long publishedEdgeCount,
        long coveredMaterialCount,
        long isolatedNodeCount,
        long missingCoreRelationNodeCount,
        List<GraphWorkbenchActivity> recentActivities,
        long pendingConflictCount) {}
