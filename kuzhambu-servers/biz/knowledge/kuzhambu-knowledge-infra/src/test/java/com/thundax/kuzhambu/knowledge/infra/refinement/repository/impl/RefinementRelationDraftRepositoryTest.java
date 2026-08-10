package com.thundax.kuzhambu.knowledge.infra.refinement.repository.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject.RefinementRelationDraftDO;
import com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper.RefinementRelationDraftMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RefinementRelationDraftRepositoryTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void listByTaskIdShouldQueryReadableTaskScope() {
        RefinementRelationDraftMapper mapper = mock(RefinementRelationDraftMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        RefinementRelationDraftRepositoryImpl repository = new RefinementRelationDraftRepositoryImpl(mapper);

        repository.listByTaskId(88L);

        ArgumentCaptor<QueryWrapper<RefinementRelationDraftDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("refinement_task_id"));
    }

    @Test
    void batchSaveOrUpdateShouldInsertWhenDraftDoesNotExist() {
        RefinementRelationDraftMapper mapper = mock(RefinementRelationDraftMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(RefinementRelationDraftDO.class))).thenReturn(1);
        RefinementRelationDraftRepositoryImpl repository = new RefinementRelationDraftRepositoryImpl(mapper);
        RefinementRelationDraft draft = new RefinementRelationDraft(
                null,
                null,
                99L,
                1L,
                "manual:relation:1",
                "MANUAL_CREATED",
                "ADDED",
                "entity:a",
                "entity:b",
                "黄帝",
                "炎帝",
                "ALLY",
                "证据",
                "PENDING",
                "[]",
                1,
                1L,
                Instant.now(),
                1L,
                Instant.now());

        repository.batchSaveOrUpdate(List.of(draft));

        verify(mapper).insert(any(RefinementRelationDraftDO.class));
    }
}
