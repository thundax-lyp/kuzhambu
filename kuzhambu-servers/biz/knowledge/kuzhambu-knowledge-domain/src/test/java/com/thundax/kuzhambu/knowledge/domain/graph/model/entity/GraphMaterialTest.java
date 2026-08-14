package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphMaterialTest {

    @Test
    void requireEditableShouldAllowDraftAndReady() {
        GraphMaterial material = material(GraphMaterialStatus.DRAFT);

        material.requireEditable();
        material.setStatus(GraphMaterialStatus.READY);
        material.requireEditable();

        assertTrue(material.editable());
    }

    @Test
    void requireEditableShouldRejectPublished() {
        GraphMaterial material = material(GraphMaterialStatus.PUBLISHED);

        assertThrows(DomainException.class, material::requireEditable);
    }

    @Test
    void publishShouldRequireReadyAndSetPublishedAt() {
        GraphMaterial material = material(GraphMaterialStatus.READY);
        Instant completedAt = Instant.parse("2026-08-13T00:00:00Z");

        material.publish(completedAt);

        assertEquals(GraphMaterialStatus.PUBLISHED, material.getStatus());
        assertEquals(completedAt, material.getPublishedAt());
    }

    @Test
    void withdrawShouldRestoreDraftWhenGraphEmpty() {
        GraphMaterial material = material(GraphMaterialStatus.PUBLISHED);
        material.setPublishedAt(Instant.parse("2026-08-13T00:00:00Z"));

        material.withdraw(true);

        assertEquals(GraphMaterialStatus.DRAFT, material.getStatus());
        assertNull(material.getPublishedAt());
    }

    @Test
    void requireLockVersionShouldRejectMismatch() {
        GraphMaterial material = material(GraphMaterialStatus.READY);
        material.setLockVersion(2L);

        assertThrows(DomainException.class, () -> material.requireLockVersion(1L));
    }

    private static GraphMaterial material(GraphMaterialStatus status) {
        GraphMaterial material = new GraphMaterial();
        material.setContentRef(new ContentRef("SANCAI_ENTRY", 1L));
        material.setStatus(status);
        return material;
    }
}
