package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import java.time.Instant;
import java.util.List;

/** Immutable workbench overview materialized from the published graph. */
public record GraphWorkbenchOverviewSnapshot(
        Instant generatedAt,
        String sourceFingerprint,
        long publishedNodeCount,
        long publishedEdgeCount,
        long coveredMaterialCount,
        long isolatedNodeCount,
        long missingCoreRelationNodeCount,
        long pendingConflictCount,
        List<GraphWorkbenchActivity> recentActivities) {

    public GraphWorkbenchOverviewSnapshot {
        recentActivities = recentActivities == null ? List.of() : List.copyOf(recentActivities);
    }
}
