package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskPersistenceAssemblerTest {

    @Test
    void shouldRoundTripTaskSnapshotsStatusAndRelations() {
        GraphExtractionTask source = new GraphExtractionTask(
                new GraphExtractionTaskId(101L),
                201L,
                new ContentRef("CLASSICS_CONTENT", 301L),
                "{\"content\":true}",
                "{\"model\":true}",
                "{\"prompt\":true}",
                "{\"schema\":true}",
                GraphExtractionExecutionStatus.SUCCEEDED,
                GraphExtractionDisposition.PENDING,
                2,
                3L,
                "batch-1",
                501L,
                401L,
                "REVIEW",
                80,
                "key-1",
                new GraphExtractionTaskId(102L),
                new GraphExtractionTaskId(103L),
                new GraphExtractionTaskId(104L),
                Instant.parse("2026-08-17T00:00:00Z"),
                Instant.parse("2026-08-17T00:01:00Z"),
                Instant.parse("2026-08-17T00:02:00Z"),
                Instant.parse("2026-08-24T00:02:00Z"));

        GraphExtractionTask restored = GraphExtractionTaskPersistenceAssembler.toDomain(
                GraphExtractionTaskPersistenceAssembler.toObject(source));

        assertEquals(source.getId(), restored.getId());
        assertEquals(source.getContentRef(), restored.getContentRef());
        assertEquals(source.getModelSnapshotJson(), restored.getModelSnapshotJson());
        assertEquals(source.getPromptSnapshotJson(), restored.getPromptSnapshotJson());
        assertEquals(source.getExecutionStatus(), restored.getExecutionStatus());
        assertEquals(source.getDisposition(), restored.getDisposition());
        assertEquals(source.getAiBatchId(), restored.getAiBatchId());
        assertEquals(source.getRegeneratedFromTaskId(), restored.getRegeneratedFromTaskId());
        assertEquals(source.getSupersededByTaskId(), restored.getSupersededByTaskId());
        assertEquals(source.getTriggeredByTaskId(), restored.getTriggeredByTaskId());
        assertEquals(source.getPurgeAfter(), restored.getPurgeAfter());
    }

    @Test
    void shouldRoundTripMaterialStats() {
        GraphMaterialStats source =
                new GraphMaterialStats(201L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, Instant.parse("2026-08-17T00:00:00Z"));

        GraphMaterialStats restored = GraphMaterialStatsPersistenceAssembler.toDomain(
                GraphMaterialStatsPersistenceAssembler.toObject(source));

        assertEquals(source.getMaterialId(), restored.getMaterialId());
        assertEquals(source.getDraftNodeCount(), restored.getDraftNodeCount());
        assertEquals(source.getPublishedEdgeCount(), restored.getPublishedEdgeCount());
        assertEquals(source.getActiveTaskCount(), restored.getActiveTaskCount());
        assertEquals(source.getStatsRevision(), restored.getStatsRevision());
        assertEquals(source.getCalculatedAt(), restored.getCalculatedAt());
    }
}
