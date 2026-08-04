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
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageNodeMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeLineageNodeRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldQueryByReadableFilters() {
        KnowledgeLineageNodeMapper mapper = mock(KnowledgeLineageNodeMapper.class);
        Page<KnowledgeLineageNodeDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new KnowledgeLineageNodeDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        KnowledgeLineageNodeRepositoryImpl repository = new KnowledgeLineageNodeRepositoryImpl(mapper);

        PageResult<KnowledgeLineageNode> page = repository.page(71L, "黄帝", "PERSON", "CONFIRMED", 1, 10);

        ArgumentCaptor<QueryWrapper<KnowledgeLineageNodeDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("latest_version_id"));
        assertTrue(sqlSegment.contains("name"));
        assertTrue(sqlSegment.contains("node_type"));
        assertTrue(sqlSegment.contains("confirmation_status"));
        assertEquals(1, page.getRecords().size());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByNodeIdShouldQueryId() {
        KnowledgeLineageNodeMapper mapper = mock(KnowledgeLineageNodeMapper.class);
        when(mapper.selectOne(any())).thenReturn(new KnowledgeLineageNodeDO());
        KnowledgeLineageNodeRepositoryImpl repository = new KnowledgeLineageNodeRepositoryImpl(mapper);

        repository.getByNodeId(3001L);

        ArgumentCaptor<QueryWrapper<KnowledgeLineageNodeDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("id"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listByNodeKeysShouldQueryByBusinessKey() {
        KnowledgeLineageNodeMapper mapper = mock(KnowledgeLineageNodeMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        KnowledgeLineageNodeRepositoryImpl repository = new KnowledgeLineageNodeRepositoryImpl(mapper);

        repository.listByNodeKeys(List.of("junzhu:huangdi"));

        ArgumentCaptor<QueryWrapper<KnowledgeLineageNodeDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectList(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("node_key"));
    }

    @Test
    void saveOrUpdateBatchShouldInsertWhenNodeDoesNotExist() {
        KnowledgeLineageNodeMapper mapper = mock(KnowledgeLineageNodeMapper.class);
        when(mapper.update(any(), any())).thenReturn(0);
        when(mapper.insert(any(KnowledgeLineageNodeDO.class))).thenReturn(1);
        KnowledgeLineageNodeRepositoryImpl repository = new KnowledgeLineageNodeRepositoryImpl(mapper);
        KnowledgeLineageNode node = new KnowledgeLineageNode();
        node.setNodeKey("junzhu:huangdi");
        node.setName("黄帝");
        node.setNodeType("RULER");
        node.setGeneration(1);
        node.setGender("MALE");
        node.setConfirmationStatus("AI_EXTRACTED");
        node.setLatestVersionId(201L);
        node.setSourceRefsJson("[{\"entryId\":1}]");
        node.setFirstExtractedAt(Instant.now());
        node.setLastExtractedAt(Instant.now());

        repository.saveOrUpdateBatch(List.of(node));

        ArgumentCaptor<KnowledgeLineageNodeDO> captor = ArgumentCaptor.forClass(KnowledgeLineageNodeDO.class);
        verify(mapper).insert(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("junzhu:huangdi", captor.getValue().getNodeKey());
    }
}
