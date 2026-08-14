package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphPublishedEdgeTest {

    @Test
    void deleteAndActivateShouldUpdateStatusAndModifiedAt() {
        GraphPublishedEdge edge = edge(GraphPublishedStatus.ACTIVE);
        Instant deletedAt = Instant.parse("2026-08-13T00:00:00Z");
        Instant activatedAt = Instant.parse("2026-08-13T01:00:00Z");

        edge.delete(deletedAt);
        edge.activate(activatedAt);

        assertEquals(GraphPublishedStatus.ACTIVE, edge.getStatus());
        assertEquals(activatedAt, edge.getModifiedAt());
    }

    @Test
    void activateShouldRejectActiveEdge() {
        GraphPublishedEdge edge = edge(GraphPublishedStatus.ACTIVE);

        assertThrows(DomainException.class, () -> edge.activate(Instant.now()));
    }

    @Test
    void refreshEdgeKeyShouldGenerateKey() {
        GraphPublishedEdge edge = edge(GraphPublishedStatus.ACTIVE);

        edge.refreshEdgeKey(new GraphNodeKey("node:a"), new GraphNodeKey("node:b"), true, Map.of());

        assertNotNull(edge.getEdgeKey());
    }

    @Test
    void requireLockVersionShouldRejectMismatch() {
        GraphPublishedEdge edge = edge(GraphPublishedStatus.ACTIVE);
        edge.setLockVersion(2L);

        assertThrows(DomainException.class, () -> edge.requireLockVersion(1L));
    }

    private static GraphPublishedEdge edge(GraphPublishedStatus status) {
        GraphPublishedEdge edge = new GraphPublishedEdge();
        edge.setSourceNodeId(new GraphPublishedNodeId(1L));
        edge.setTargetNodeId(new GraphPublishedNodeId(2L));
        edge.setRelationType("ASSOCIATED_WITH");
        edge.setSource(GraphSourceType.MANUAL);
        edge.setStatus(status);
        return edge;
    }
}
