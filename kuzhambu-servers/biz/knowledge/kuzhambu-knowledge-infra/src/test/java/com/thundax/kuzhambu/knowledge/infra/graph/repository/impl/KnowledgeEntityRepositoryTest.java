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
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.KnowledgeEntityIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.KnowledgeEntityDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper.KnowledgeEntityMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeEntityRepositoryTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void pageShouldQueryByReadableFilters() {
        KnowledgeEntityMapper mapper = mock(KnowledgeEntityMapper.class);
        Page<KnowledgeEntityDO> dataObjectPage = new Page<>(1, 10, 1);
        dataObjectPage.setRecords(List.of(new KnowledgeEntityDO()));
        when(mapper.selectPage(any(Page.class), any())).thenReturn(dataObjectPage);
        KnowledgeEntityRepositoryImpl repository = new KnowledgeEntityRepositoryImpl(mapper);

        PageResult<KnowledgeEntity> page = repository.page(
                GraphVersionIdCodec.toDomain(71L), "黄帝", "PERSON", KnowledgeConfirmationStatus.CONFIRMED, 1, 10);

        ArgumentCaptor<QueryWrapper<KnowledgeEntityDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectPage(any(Page.class), captor.capture());
        String sqlSegment = captor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("latest_version_id"));
        assertTrue(sqlSegment.contains("name"));
        assertTrue(sqlSegment.contains("entity_type"));
        assertTrue(sqlSegment.contains("confirmation_status"));
        assertEquals(1, page.getRecords().size());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByEntityIdShouldQueryPrimaryKey() {
        KnowledgeEntityMapper mapper = mock(KnowledgeEntityMapper.class);
        when(mapper.selectOne(any())).thenReturn(new KnowledgeEntityDO());
        KnowledgeEntityRepositoryImpl repository = new KnowledgeEntityRepositoryImpl(mapper);

        repository.getByEntityId(KnowledgeEntityIdCodec.toDomain(1001L));

        ArgumentCaptor<QueryWrapper<KnowledgeEntityDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("id"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getByEntityKeyShouldQueryReadableKey() {
        KnowledgeEntityMapper mapper = mock(KnowledgeEntityMapper.class);
        when(mapper.selectOne(any())).thenReturn(new KnowledgeEntityDO());
        KnowledgeEntityRepositoryImpl repository = new KnowledgeEntityRepositoryImpl(mapper);

        repository.getByEntityKey("person:huangdi");

        ArgumentCaptor<QueryWrapper<KnowledgeEntityDO>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOne(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("entity_key"));
    }

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
        entity.setConfirmationStatus(KnowledgeConfirmationStatus.AI_EXTRACTED);
        entity.setLatestVersionId(GraphVersionIdCodec.toDomain(101L));
        entity.setSourceRefsJson("[{\"entryId\":1}]");
        entity.setFirstExtractedAt(Instant.now());
        entity.setLastExtractedAt(Instant.now());

        repository.saveOrUpdateBatch(List.of(entity));

        ArgumentCaptor<KnowledgeEntityDO> captor = ArgumentCaptor.forClass(KnowledgeEntityDO.class);
        verify(mapper).insert(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals("renwu:huangdi", captor.getValue().getEntityKey());
    }
}
