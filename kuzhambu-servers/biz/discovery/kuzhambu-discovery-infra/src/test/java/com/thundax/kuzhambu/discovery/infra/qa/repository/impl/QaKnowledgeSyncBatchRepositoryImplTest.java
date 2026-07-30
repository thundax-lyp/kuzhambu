package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncBatch;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaKnowledgeSyncBatchDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaKnowledgeSyncBatchMapper;
import java.util.Date;
import org.junit.jupiter.api.Test;

class QaKnowledgeSyncBatchRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifierWhenIdIsMissing() {
        QaKnowledgeSyncBatchMapper mapper = mock(QaKnowledgeSyncBatchMapper.class);
        QaKnowledgeSyncBatchRepositoryImpl repository = new QaKnowledgeSyncBatchRepositoryImpl(mapper);
        QaKnowledgeSyncBatch entity =
                new QaKnowledgeSyncBatch(null, 9001L, "FULL_REBUILD", "fastgpt", 10, 8, 2, new Date(), new Date());
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaKnowledgeSyncBatchDO.class).setId(9001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaKnowledgeSyncBatchDO.class));

        Long savedId = repository.save(entity);

        assertEquals(9001L, savedId);
        verify(mapper).insert(any(QaKnowledgeSyncBatchDO.class));
    }

    @Test
    void getByBatchIdShouldReturnBatch() {
        QaKnowledgeSyncBatchMapper mapper = mock(QaKnowledgeSyncBatchMapper.class);
        QaKnowledgeSyncBatchRepositoryImpl repository = new QaKnowledgeSyncBatchRepositoryImpl(mapper);
        QaKnowledgeSyncBatchDO dataObject =
                new QaKnowledgeSyncBatchDO(9001L, "FULL_REBUILD", "fastgpt", 10, 8, 2, new Date(), new Date());
        when(mapper.selectById(9001L)).thenReturn(dataObject);

        QaKnowledgeSyncBatch result = repository.getByBatchId(9001L);

        assertEquals(9001L, result.getId());
        assertEquals("fastgpt", result.getProvider());
        assertEquals(10, result.getTotalCount());
    }
}
