package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementLineageNodeDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementLineageNodeDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementLineageNodeDraftMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RefinementLineageNodeDraftRepositoryTest {

    @Test
    void saveOrUpdateBatchShouldInsertWhenDraftDoesNotExist() {
        RefinementLineageNodeDraftMapper mapper = mock(RefinementLineageNodeDraftMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(RefinementLineageNodeDraftDO.class))).thenReturn(1);
        RefinementLineageNodeDraftRepositoryImpl repository = new RefinementLineageNodeDraftRepositoryImpl(mapper);
        RefinementLineageNodeDraft draft = new RefinementLineageNodeDraft(
                null,
                null,
                99L,
                1L,
                "manual:lineage-node:1",
                "MANUAL_CREATED",
                "ADDED",
                "黄帝",
                "KING",
                1,
                "MALE",
                "PENDING",
                "[]",
                1,
                1L,
                Instant.now(),
                1L,
                Instant.now());

        repository.saveOrUpdateBatch(List.of(draft));

        verify(mapper).insert(any(RefinementLineageNodeDraftDO.class));
    }
}
