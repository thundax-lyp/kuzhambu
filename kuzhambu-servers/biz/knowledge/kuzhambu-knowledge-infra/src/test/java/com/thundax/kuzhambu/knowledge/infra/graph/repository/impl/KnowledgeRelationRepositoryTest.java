package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeRelationMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeRelationRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldQueryByReadableFilters() {
        KnowledgeRelationMapper mapper = mock(KnowledgeRelationMapper.class);
        Page<KnowledgeRelationDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new KnowledgeRelationDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        KnowledgeRelationRepositoryImpl repository = new KnowledgeRelationRepositoryImpl(mapper);

        PageResult<KnowledgeRelation> page = repository.page(71L, "黄帝", "ANCESTOR", "CONFIRMED", 1, 10);

        ArgumentCaptor<QueryWrapper<KnowledgeRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("latest_version_id"));
        assertTrue(sqlSegment.contains("source_name"));
        assertTrue(sqlSegment.contains("target_name"));
        assertTrue(sqlSegment.contains("relation_type"));
        assertTrue(sqlSegment.contains("confirmation_status"));
        assertEquals(1, page.getRecords().size());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByRelationIdShouldQueryId() {
        KnowledgeRelationMapper mapper = mock(KnowledgeRelationMapper.class);
        when(mapper.selectOne(any())).thenReturn(new KnowledgeRelationDO());
        KnowledgeRelationRepositoryImpl repository = new KnowledgeRelationRepositoryImpl(mapper);

        repository.getByRelationId(2001L);

        ArgumentCaptor<QueryWrapper<KnowledgeRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("id"));
    }

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
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByEntityKeyShouldQueryRelationAdjacency() {
        KnowledgeRelationMapper mapper = mock(KnowledgeRelationMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(new KnowledgeRelationDO()));
        KnowledgeRelationRepositoryImpl repository = new KnowledgeRelationRepositoryImpl(mapper);

        List<KnowledgeRelation> result = repository.listByEntityKey("person:huangdi");

        ArgumentCaptor<QueryWrapper<KnowledgeRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("source_entity_key"));
        assertTrue(sqlSegment.contains("target_entity_key"));
        assertEquals(1, result.size());
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
        relation.setFirstExtractedAt(Instant.now());
        relation.setLastExtractedAt(Instant.now());

        repository.saveOrUpdateBatch(List.of(relation));

        ArgumentCaptor<KnowledgeRelationDO> captor = ArgumentCaptor.forClass(KnowledgeRelationDO.class);
        verify(mapper).insert(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("renwu:huangdi->fuxi:ancestor", captor.getValue().getRelationKey());
    }
}
