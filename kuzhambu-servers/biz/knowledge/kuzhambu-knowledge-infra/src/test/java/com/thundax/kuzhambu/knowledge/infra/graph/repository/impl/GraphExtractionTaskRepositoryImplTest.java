package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphExtractionTaskDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.GraphExtractionTaskMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphExtractionTaskRepositoryImplTest {

    @Test
    void shouldDelegateMaterialBatchPurgeAndOptimisticLockQueries() {
        GraphExtractionTaskMapper mapper = mock(GraphExtractionTaskMapper.class);
        GraphExtractionTaskDO task = task(101L, 201L, "batch-1", Instant.parse("2026-08-24T00:00:00Z"));
        when(mapper.selectByMaterialId(201L)).thenReturn(List.of(task));
        when(mapper.selectByBatchId("batch-1")).thenReturn(List.of(task));
        when(mapper.selectPurgeableBefore(Instant.parse("2026-08-25T00:00:00Z"), 100))
                .thenReturn(List.of(task));
        when(mapper.updateIfLockVersion(any(), eq(3L))).thenReturn(1);
        GraphExtractionTaskRepositoryImpl repository = new GraphExtractionTaskRepositoryImpl(mapper);

        assertEquals(101L, repository.listByMaterialId(201L).get(0).getId().value());
        assertEquals("batch-1", repository.listByBatchId("batch-1").get(0).getBatchId());
        assertEquals(
                101L,
                repository
                        .listPurgeableBefore(Instant.parse("2026-08-25T00:00:00Z"), 0)
                        .get(0)
                        .getId()
                        .value());
        GraphExtractionTask update = new GraphExtractionTask();
        update.setId(new GraphExtractionTaskId(101L));
        update.setLockVersion(3L);
        assertEquals(1, repository.updateIfLockVersion(update, 3L));
        verify(mapper).updateIfLockVersion(any(), eq(3L));
    }

    private static GraphExtractionTaskDO task(Long id, Long materialId, String batchId, Instant purgeAfter) {
        GraphExtractionTaskDO dataObject = new GraphExtractionTaskDO();
        dataObject.setId(id);
        dataObject.setMaterialId(materialId);
        dataObject.setContentType("CLASSICS_CONTENT");
        dataObject.setContentRefId(301L);
        dataObject.setContentSnapshotJson("{\"content\":true}");
        dataObject.setExecutionStatus(GraphExtractionExecutionStatus.SUCCEEDED.value());
        dataObject.setAttemptNo(1);
        dataObject.setLockVersion(3L);
        dataObject.setBatchId(batchId);
        dataObject.setProgress(100);
        dataObject.setRequestedAt(Instant.parse("2026-08-17T00:00:00Z"));
        dataObject.setPurgeAfter(purgeAfter);
        return dataObject;
    }
}
