package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementEntityDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementEntityDraftMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefinementEntityDraftRepositoryTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listByTaskIdShouldQueryReadableTaskScope() {
        RefinementEntityDraftMapper mapper = mock(RefinementEntityDraftMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        RefinementEntityDraftRepositoryImpl repository = new RefinementEntityDraftRepositoryImpl(mapper);

        repository.listByTaskId(88L);

        ArgumentCaptor<QueryWrapper<RefinementEntityDraftDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("refinement_task_id"));
    }

    @Test
    void saveOrUpdateBatchShouldInsertWhenDraftDoesNotExist() {
        RefinementEntityDraftMapper mapper = mock(RefinementEntityDraftMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(RefinementEntityDraftDO.class))).thenReturn(1);
        RefinementEntityDraftRepositoryImpl repository = new RefinementEntityDraftRepositoryImpl(mapper);
        RefinementEntityDraft draft = new RefinementEntityDraft(
                null,
                null,
                99L,
                1L,
                "manual:entity:1",
                "MANUAL_CREATED",
                "ADDED",
                "黄帝",
                "PERSON",
                "说明",
                "PENDING",
                "[]",
                1,
                1L,
                Instant.now(),
                1L,
                Instant.now());

        repository.saveOrUpdateBatch(List.of(draft));

        verify(mapper).insert(any(RefinementEntityDraftDO.class));
    }
}
