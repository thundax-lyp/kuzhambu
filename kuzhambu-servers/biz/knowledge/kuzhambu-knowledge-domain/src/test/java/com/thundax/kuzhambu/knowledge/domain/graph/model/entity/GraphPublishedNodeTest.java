package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphPublishedNodeTest {

    @Test
    void deleteAndActivateShouldUpdateStatusAndModifiedAt() {
        GraphPublishedNode node = node(GraphPublishedStatus.ACTIVE);
        Instant deletedAt = Instant.parse("2026-08-13T00:00:00Z");
        Instant activatedAt = Instant.parse("2026-08-13T01:00:00Z");

        node.delete(deletedAt);
        node.activate(activatedAt);

        assertEquals(GraphPublishedStatus.ACTIVE, node.getStatus());
        assertEquals(activatedAt, node.getModifiedAt());
    }

    @Test
    void deleteShouldRejectDeletedNode() {
        GraphPublishedNode node = node(GraphPublishedStatus.DELETED);

        assertThrows(DomainException.class, () -> node.delete(Instant.now()));
    }

    @Test
    void refreshNodeKeyShouldValidateRequiredFields() {
        GraphPublishedNode node = node(GraphPublishedStatus.ACTIVE);

        node.refreshNodeKey("唐");

        assertNotNull(node.getNodeKey());
    }

    @Test
    void requireLockVersionShouldRejectMismatch() {
        GraphPublishedNode node = node(GraphPublishedStatus.ACTIVE);
        node.setLockVersion(2L);

        assertThrows(DomainException.class, () -> node.requireLockVersion(1L));
    }

    private static GraphPublishedNode node(GraphPublishedStatus status) {
        GraphPublishedNode node = new GraphPublishedNode();
        node.setNodeType(GraphNodeType.PERSON);
        node.setName("李白");
        node.setSource(GraphSourceType.MANUAL);
        node.setStatus(status);
        return node;
    }
}
