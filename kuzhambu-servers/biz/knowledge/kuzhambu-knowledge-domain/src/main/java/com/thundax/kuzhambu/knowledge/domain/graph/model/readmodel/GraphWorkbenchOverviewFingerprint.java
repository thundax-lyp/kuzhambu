package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

/** Lightweight change markers for deciding whether the workbench overview snapshot must be rebuilt. */
public record GraphWorkbenchOverviewFingerprint(
        long activeNodeCount,
        Long activeNodeModifiedAt,
        long activeEdgeCount,
        Long activeEdgeModifiedAt,
        long nodeMaterialAssociationCount,
        Long nodeMaterialAssociationChangedAt,
        long edgeMaterialAssociationCount,
        Long edgeMaterialAssociationChangedAt,
        long publicationCount,
        Long publicationOccurredAt,
        long governanceOperationCount,
        Long governanceOperationOccurredAt,
        long deletionChangeCount,
        Long deletionChangeOccurredAt,
        long pendingConflictCount,
        Long nextPendingConflictExpiresAt,
        String schemaFingerprint) {}
