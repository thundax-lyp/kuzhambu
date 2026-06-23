package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeEntityMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeEntityRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByEntityKeysShouldQueryByBusinessKey() {
        KnowledgeEntityMapper mapper = mock(KnowledgeEntityMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        KnowledgeEntityRepositoryImpl repository = new KnowledgeEntityRepositoryImpl(mapper);

        repository.listByEntityKeys(List.of("renwu:huangdi"));

        ArgumentCaptor<QueryWrapper<KnowledgeEntityDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("entity_key"));
    }

    @Test
    void saveOrUpdateBatchShouldInsertWhenEntityDoesNotExist() {
        KnowledgeEntityMapper mapper = mock(KnowledgeEntityMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(KnowledgeEntityDO.class))).thenReturn(1);
        KnowledgeEntityRepositoryImpl repository = new KnowledgeEntityRepositoryImpl(mapper);
        KnowledgeEntity entity = new KnowledgeEntity();
        entity.setEntityKey("renwu:huangdi");
        entity.setName("黄帝");
        entity.setEntityType("PERSON");
        entity.setDescription("人皇");
        entity.setConfirmationStatus("AI_EXTRACTED");
        entity.setLatestVersionId(101L);
        entity.setSourceRefsJson("[{\"entryId\":1}]");
        entity.setFirstExtractedAt(new Date());
        entity.setLastExtractedAt(new Date());

        repository.saveOrUpdateBatch(List.of(entity));

        ArgumentCaptor<KnowledgeEntityDO> captor = ArgumentCaptor.forClass(KnowledgeEntityDO.class);
        verify(mapper).insert(captor.capture());
        assertNotNull(captor.getValue().getEntityId());
        assertEquals("renwu:huangdi", captor.getValue().getEntityKey());
    }
}
