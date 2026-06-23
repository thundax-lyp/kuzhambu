package com.thundax.kuzhambu.knowledge.infra.graph.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeLineageNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeLineageNodeMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeLineageNodeRepositoryTest {

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
        when(mapper.insert(any())).thenReturn(1);
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
        node.setFirstExtractedAt(new Date());
        node.setLastExtractedAt(new Date());

        repository.saveOrUpdateBatch(List.of(node));

        ArgumentCaptor<KnowledgeLineageNodeDO> captor = ArgumentCaptor.forClass(KnowledgeLineageNodeDO.class);
        verify(mapper).insert(captor.capture());
        assertNotNull(captor.getValue().getNodeId());
        assertEquals("junzhu:huangdi", captor.getValue().getNodeKey());
    }
}
