package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaRetrievalTraceMapper;
import java.util.Date;
import org.junit.jupiter.api.Test;

class QaRetrievalTraceRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenTraceIdIsMissing() {
        QaRetrievalTraceMapper mapper = mock(QaRetrievalTraceMapper.class);
        QaRetrievalTraceRepositoryImpl repository = new QaRetrievalTraceRepositoryImpl(mapper);
        QaRetrievalTrace entity = new QaRetrievalTrace(
                null, null, 2001L, "黄帝是谁", "黄帝 传说人物", "GLOBAL", "{}", "[\"轩辕\"]", "[]", 3, "context", new Date());

        Long savedId = repository.save(entity);

        assertNotNull(savedId);
        verify(mapper).insert(any(QaRetrievalTraceDO.class));
    }

    @Test
    void getByMessageIdShouldReturnStoredTrace() {
        QaRetrievalTraceMapper mapper = mock(QaRetrievalTraceMapper.class);
        QaRetrievalTraceRepositoryImpl repository = new QaRetrievalTraceRepositoryImpl(mapper);
        QaRetrievalTraceDO dataObject = new QaRetrievalTraceDO(
                1L, 5001L, 2001L, "黄帝是谁", "黄帝 传说人物", "GLOBAL", "{}", "[\"轩辕\"]", "[]", 3, "context", new Date());
        when(mapper.selectOne(any())).thenReturn(dataObject);

        QaRetrievalTrace result = repository.getByMessageId(2001L);

        assertEquals(5001L, result.getTraceId());
        assertEquals("黄帝 传说人物", result.getRewrittenQuestion());
    }
}
