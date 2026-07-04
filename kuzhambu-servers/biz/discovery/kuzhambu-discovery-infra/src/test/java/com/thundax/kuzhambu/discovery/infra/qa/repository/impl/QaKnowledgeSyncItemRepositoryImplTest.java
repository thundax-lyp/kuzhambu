package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaKnowledgeSyncItemDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaKnowledgeSyncItemMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaKnowledgeSyncItemRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifierWhenIdIsMissing() {
        QaKnowledgeSyncItemMapper mapper = mock(QaKnowledgeSyncItemMapper.class);
        QaKnowledgeSyncItemRepositoryImpl repository = new QaKnowledgeSyncItemRepositoryImpl(mapper);
        QaKnowledgeSyncItem entity = new QaKnowledgeSyncItem(
                null,
                "SANCAI:1001",
                "SANCAI_ENTRY",
                1001L,
                "kuzhambu-qa",
                1,
                "rev-1001",
                "fastgpt",
                "kb-1",
                "item-1",
                "PENDING",
                null,
                null,
                new Date(),
                new Date());

        Long savedId = repository.save(entity);

        assertNotNull(savedId);
        verify(mapper).insert(any(QaKnowledgeSyncItemDO.class));
    }

    @Test
    void getBySourceIdShouldReturnItem() {
        QaKnowledgeSyncItemMapper mapper = mock(QaKnowledgeSyncItemMapper.class);
        QaKnowledgeSyncItemRepositoryImpl repository = new QaKnowledgeSyncItemRepositoryImpl(mapper);
        QaKnowledgeSyncItemDO dataObject = new QaKnowledgeSyncItemDO(
                1L,
                "SANCAI:1001",
                "SANCAI_ENTRY",
                1001L,
                "kuzhambu-qa",
                1,
                "rev-1001",
                "fastgpt",
                "kb-1",
                "item-1",
                "SUCCEEDED",
                null,
                new Date(),
                new Date(),
                new Date());
        when(mapper.selectOne(any())).thenReturn(dataObject);

        QaKnowledgeSyncItem result = repository.getBySourceId("SANCAI:1001");

        assertEquals("SANCAI:1001", result.getSourceId());
        assertEquals("SUCCEEDED", result.getSyncStatus());
    }

    @Test
    void listBySyncStatusShouldRespectLimit() {
        QaKnowledgeSyncItemMapper mapper = mock(QaKnowledgeSyncItemMapper.class);
        QaKnowledgeSyncItemRepositoryImpl repository = new QaKnowledgeSyncItemRepositoryImpl(mapper);
        QaKnowledgeSyncItemDO first = new QaKnowledgeSyncItemDO(
                1L,
                "SANCAI:1001",
                "SANCAI_ENTRY",
                1001L,
                "kuzhambu-qa",
                1,
                "rev-1001",
                "fastgpt",
                "kb-1",
                "item-1",
                "FAILED",
                null,
                null,
                new Date(),
                new Date());
        QaKnowledgeSyncItemDO second = new QaKnowledgeSyncItemDO(
                2L,
                "WANGQI:2001",
                "WANGQI_DOCUMENT",
                2001L,
                "kuzhambu-qa",
                2,
                "rev-2001",
                "fastgpt",
                "kb-1",
                "item-2",
                "FAILED",
                "sync timeout",
                null,
                new Date(),
                new Date());
        when(mapper.selectList(any())).thenReturn(List.of(first, second));

        List<QaKnowledgeSyncItem> result = repository.listBySyncStatus("FAILED", 2);

        assertEquals(2, result.size());
        assertEquals("SANCAI:1001", result.get(0).getSourceId());
        assertEquals("WANGQI:2001", result.get(1).getSourceId());
    }
}
