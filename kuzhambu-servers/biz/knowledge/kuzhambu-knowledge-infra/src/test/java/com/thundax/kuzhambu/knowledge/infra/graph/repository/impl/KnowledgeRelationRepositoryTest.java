package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeRelationMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeRelationRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByRelationKeysShouldQueryByBusinessKey() {
        KnowledgeRelationMapper mapper = mock(KnowledgeRelationMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        KnowledgeRelationRepositoryImpl repository = new KnowledgeRelationRepositoryImpl(mapper);

        repository.listByRelationKeys(List.of("renwu:huangdi->fuxi:ancestor"));

        ArgumentCaptor<QueryWrapper<KnowledgeRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("relation_key"));
    }

    @Test
    void saveOrUpdateBatchShouldInsertWhenRelationDoesNotExist() {
        KnowledgeRelationMapper mapper = mock(KnowledgeRelationMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(KnowledgeRelationDO.class))).thenReturn(1);
        KnowledgeRelationRepositoryImpl repository = new KnowledgeRelationRepositoryImpl(mapper);
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setRelationKey("renwu:huangdi->fuxi:ancestor");
        relation.setSourceEntityKey("renwu:huangdi");
        relation.setTargetEntityKey("renwu:fuxi");
        relation.setSourceName("黄帝");
        relation.setTargetName("伏羲");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("谱系记载");
        relation.setConfirmationStatus("AI_EXTRACTED");
        relation.setLatestVersionId(102L);
        relation.setSourceRefsJson("[{\"entryId\":1}]");
        relation.setFirstExtractedAt(new Date());
        relation.setLastExtractedAt(new Date());

        repository.saveOrUpdateBatch(List.of(relation));

        ArgumentCaptor<KnowledgeRelationDO> captor = ArgumentCaptor.forClass(KnowledgeRelationDO.class);
        verify(mapper).insert(captor.capture());
        assertNotNull(captor.getValue().getRelationId());
        assertEquals("renwu:huangdi->fuxi:ancestor", captor.getValue().getRelationKey());
    }
}
