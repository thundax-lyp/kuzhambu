package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageRelationDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageRelationDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementLineageRelationDraftMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RefinementLineageRelationDraftRepositoryTest {

    @Test
    void saveOrUpdateBatchShouldInsertWhenDraftDoesNotExist() {
        RefinementLineageRelationDraftMapper mapper = mock(RefinementLineageRelationDraftMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(RefinementLineageRelationDraftDO.class))).thenReturn(1);
        RefinementLineageRelationDraftRepositoryImpl repository =
                new RefinementLineageRelationDraftRepositoryImpl(mapper);
        RefinementLineageRelationDraft draft = new RefinementLineageRelationDraft(
                null,
                null,
                99L,
                1L,
                "manual:lineage-relation:1",
                "MANUAL_CREATED",
                "ADDED",
                "node:a",
                "node:b",
                "黄帝",
                "少昊",
                "PARENT",
                "证据",
                "PENDING",
                "[]",
                1,
                1L,
                Instant.now(),
                1L,
                Instant.now());

        repository.saveOrUpdateBatch(List.of(draft));

        verify(mapper).insert(any(RefinementLineageRelationDraftDO.class));
    }
}
