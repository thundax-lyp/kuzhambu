package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import org.junit.jupiter.api.Test;

class GraphMaterialEventTest {

    @Test
    void processingEventCanSucceed() {
        GraphMaterialEvent event = event(GraphMaterialEventStatus.SCHEDULED);

        event.startProcessing();
        event.succeed();

        assertEquals(GraphMaterialEventStatus.SUCCEEDED, event.getStatus());
        assertNotNull(event.getChangedAt());
    }

    @Test
    void failedEventCanScheduleRetry() {
        GraphMaterialEvent event = event(GraphMaterialEventStatus.PROCESSING);

        event.fail();
        event.scheduleRetry();

        assertEquals(GraphMaterialEventStatus.SCHEDULED, event.getStatus());
    }

    @Test
    void succeedShouldRejectScheduledEvent() {
        GraphMaterialEvent event = event(GraphMaterialEventStatus.SCHEDULED);

        assertThrows(DomainException.class, event::succeed);
    }

    @Test
    void requireLockVersionShouldRejectMismatch() {
        GraphMaterialEvent event = event(GraphMaterialEventStatus.SCHEDULED);
        event.setLockVersion(2L);

        assertThrows(DomainException.class, () -> event.requireLockVersion(1L));
    }

    private static GraphMaterialEvent event(GraphMaterialEventStatus status) {
        GraphMaterialEvent event = new GraphMaterialEvent();
        event.setMaterialRef(new ContentRef("SANCAI_ENTRY", 1L));
        event.setType(GraphMaterialEventType.DELETED);
        event.setStatus(status);
        return event;
    }
}
