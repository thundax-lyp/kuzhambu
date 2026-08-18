package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        assertTrue(material.editable());
        material.setStatus(GraphMaterialStatus.READY);
        assertTrue(material.editable());
        material.setStatus(GraphMaterialStatus.PUBLISHED);
        assertFalse(material.editable());
    }

    @Test
    void requireEditableShouldRejectNonDraftStatus() {
        GraphMaterial material = material(GraphMaterialStatus.PUBLISHING);

        assertThrows(DomainException.class, material::requireEditable);
    }

    @Test
    void refreshStatusShouldReflectWhetherTheDraftHasNodes() {
        GraphMaterial material = material(GraphMaterialStatus.DRAFT);

        material.refreshStatus(false);
        assertEquals(GraphMaterialStatus.READY, material.getStatus());

        material.refreshStatus(true);
        assertEquals(GraphMaterialStatus.DRAFT, material.getStatus());
    }

    @Test
    void publishShouldMoveReadyThroughPublishingAndSetPublishedAt() {
        GraphMaterial material = material(GraphMaterialStatus.READY);
        Instant completedAt = Instant.parse("2026-08-13T00:00:00Z");

        material.startPublishing();
        material.publish(completedAt);

        assertEquals(GraphMaterialStatus.PUBLISHED, material.getStatus());
        assertEquals(completedAt, material.getPublishedAt());
        assertNull(material.getFailureReason());
        assertNull(material.getFailedOperation());
    }

    @Test
    void withdrawShouldMovePublishedThroughWithdrawingAndRestoreDraft() {
        GraphMaterial material = material(GraphMaterialStatus.PUBLISHED);
        material.setPublishedAt(Instant.parse("2026-08-13T00:00:00Z"));

        material.startWithdrawal();
        material.withdraw();

        assertEquals(GraphMaterialStatus.DRAFT, material.getStatus());
        assertNull(material.getPublishedAt());
        assertNull(material.getFailureReason());
        assertNull(material.getFailedOperation());
    }

    @Test
    void publishFailureShouldRecordFailedOperationAndRetryToDraft() {
        GraphMaterial material = material(GraphMaterialStatus.READY);

        material.startPublishing();
        material.failPublication("schema invalid");

        assertEquals(GraphMaterialStatus.FAILED, material.getStatus());
        assertEquals("schema invalid", material.getFailureReason());
        assertEquals(GraphMaterial.FAILED_OPERATION_PUBLISH, material.getFailedOperation());

        material.retryFailure();

        assertEquals(GraphMaterialStatus.DRAFT, material.getStatus());
        assertNull(material.getFailureReason());
        assertNull(material.getFailedOperation());
    }

    @Test
    void withdrawFailureShouldRecordFailedOperationAndRetryToPublished() {
        GraphMaterial material = material(GraphMaterialStatus.PUBLISHED);

        material.startWithdrawal();
        material.failWithdrawal("mapping delete failed");

        assertEquals(GraphMaterialStatus.FAILED, material.getStatus());
        assertEquals("mapping delete failed", material.getFailureReason());
        assertEquals(GraphMaterial.FAILED_OPERATION_WITHDRAW, material.getFailedOperation());

        material.retryFailure();

        assertEquals(GraphMaterialStatus.PUBLISHED, material.getStatus());
        assertNull(material.getFailureReason());
        assertNull(material.getFailedOperation());
    }

    @Test
    void statusPersistentValuesShouldIncludeReady() {
        assertEquals("DRAFT", GraphMaterialStatus.DRAFT.value());
        assertEquals("READY", GraphMaterialStatus.READY.value());
        assertEquals("PUBLISHING", GraphMaterialStatus.PUBLISHING.value());
        assertEquals("PUBLISHED", GraphMaterialStatus.PUBLISHED.value());
        assertEquals("WITHDRAWING", GraphMaterialStatus.WITHDRAWING.value());
        assertEquals("FAILED", GraphMaterialStatus.FAILED.value());
        assertEquals(GraphMaterialStatus.READY, GraphMaterialStatus.from("READY"));
    }

    @Test
    void requireLockVersionShouldRejectMismatch() {
        GraphMaterial material = material(GraphMaterialStatus.DRAFT);
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
