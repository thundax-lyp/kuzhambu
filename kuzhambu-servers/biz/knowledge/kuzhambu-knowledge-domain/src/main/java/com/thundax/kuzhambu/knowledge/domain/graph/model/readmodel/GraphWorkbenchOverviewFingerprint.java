package com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel;

import java.time.Instant;
import java.util.StringJoiner;

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
        String schemaFingerprint) {

    public String value() {
        return new StringJoiner("|")
                .add(Long.toString(activeNodeCount))
                .add(String.valueOf(activeNodeModifiedAt))
                .add(Long.toString(activeEdgeCount))
                .add(String.valueOf(activeEdgeModifiedAt))
                .add(Long.toString(nodeMaterialAssociationCount))
                .add(String.valueOf(nodeMaterialAssociationChangedAt))
                .add(Long.toString(edgeMaterialAssociationCount))
                .add(String.valueOf(edgeMaterialAssociationChangedAt))
                .add(Long.toString(publicationCount))
                .add(String.valueOf(publicationOccurredAt))
                .add(Long.toString(governanceOperationCount))
                .add(String.valueOf(governanceOperationOccurredAt))
                .add(Long.toString(deletionChangeCount))
                .add(String.valueOf(deletionChangeOccurredAt))
                .add(Long.toString(pendingConflictCount))
                .add(String.valueOf(nextPendingConflictExpiresAt))
                .add(String.valueOf(schemaFingerprint))
                .toString();
    }

    public Instant nextRefreshAt() {
        return nextPendingConflictExpiresAt == null ? null : Instant.ofEpochMilli(nextPendingConflictExpiresAt);
    }
}
