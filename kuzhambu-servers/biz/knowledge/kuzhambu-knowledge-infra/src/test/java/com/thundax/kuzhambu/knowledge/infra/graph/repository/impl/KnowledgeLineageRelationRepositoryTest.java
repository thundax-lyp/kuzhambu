package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageRelationDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageRelationMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeLineageRelationRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldQueryByReadableFilters() {
        KnowledgeLineageRelationMapper mapper = mock(KnowledgeLineageRelationMapper.class);
        Page<KnowledgeLineageRelationDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new KnowledgeLineageRelationDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        KnowledgeLineageRelationRepositoryImpl repository = new KnowledgeLineageRelationRepositoryImpl(mapper);

        PageResult<KnowledgeLineageRelation> page = repository.page(71L, "黄帝", "ANCESTOR", "CONFIRMED", 1, 10);

        ArgumentCaptor<QueryWrapper<KnowledgeLineageRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
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
        KnowledgeLineageRelationMapper mapper = mock(KnowledgeLineageRelationMapper.class);
        when(mapper.selectOne(any())).thenReturn(new KnowledgeLineageRelationDO());
        KnowledgeLineageRelationRepositoryImpl repository = new KnowledgeLineageRelationRepositoryImpl(mapper);

        repository.getByRelationId(4001L);

        ArgumentCaptor<QueryWrapper<KnowledgeLineageRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("id"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByRelationKeysShouldQueryByBusinessKey() {
        KnowledgeLineageRelationMapper mapper = mock(KnowledgeLineageRelationMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        KnowledgeLineageRelationRepositoryImpl repository = new KnowledgeLineageRelationRepositoryImpl(mapper);

        repository.listByRelationKeys(List.of("junzhu:huangdi->junzhu:fuxi:ancestor"));

        ArgumentCaptor<QueryWrapper<KnowledgeLineageRelationDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("relation_key"));
    }

    @Test
    void saveOrUpdateBatchShouldInsertWhenRelationDoesNotExist() {
        KnowledgeLineageRelationMapper mapper = mock(KnowledgeLineageRelationMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(KnowledgeLineageRelationDO.class))).thenReturn(1);
        KnowledgeLineageRelationRepositoryImpl repository = new KnowledgeLineageRelationRepositoryImpl(mapper);
        KnowledgeLineageRelation relation = new KnowledgeLineageRelation();
        relation.setRelationKey("junzhu:huangdi->junzhu:fuxi:ancestor");
        relation.setSourceNodeKey("junzhu:huangdi");
        relation.setTargetNodeKey("junzhu:fuxi");
        relation.setSourceName("黄帝");
        relation.setTargetName("伏羲");
        relation.setRelationType("ANCESTOR");
        relation.setEvidence("世系记载");
        relation.setConfirmationStatus("AI_EXTRACTED");
        relation.setLatestVersionId(202L);
        relation.setSourceRefsJson("[{\"entryId\":1}]");
        relation.setFirstExtractedAt(Instant.now());
        relation.setLastExtractedAt(Instant.now());

        repository.saveOrUpdateBatch(List.of(relation));

        ArgumentCaptor<KnowledgeLineageRelationDO> captor = ArgumentCaptor.forClass(KnowledgeLineageRelationDO.class);
        verify(mapper).insert(captor.capture());
        assertNotNull(captor.getValue().getId());
        assertEquals("junzhu:huangdi->junzhu:fuxi:ancestor", captor.getValue().getRelationKey());
    }
}
