package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaRetrievalTraceDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaRetrievalTraceMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QaRetrievalTraceRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenTraceIdIsMissing() {
        QaRetrievalTraceMapper mapper = mock(QaRetrievalTraceMapper.class);
        QaRetrievalTraceRepositoryImpl repository = new QaRetrievalTraceRepositoryImpl(mapper);
        QaRetrievalTrace entity = new QaRetrievalTrace(
                null,
                null,
                2001L,
                "黄帝是谁",
                "fastgpt",
                "kb-1",
                "[\"doc-1\"]",
                "chat-1",
                "req-1",
                120L,
                null,
                "{\"answer\":\"黄帝 传说人物\"}",
                9001L,
                "SUCCEEDED",
                null,
                null,
                Instant.now());
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaRetrievalTraceDO.class).setId(5001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaRetrievalTraceDO.class));

        Long savedId = repository.save(entity);

        assertEquals(5001L, savedId);
        verify(mapper).insert(any(QaRetrievalTraceDO.class));
    }

    @Test
    void getByMessageIdShouldReturnStoredTrace() {
        QaRetrievalTraceMapper mapper = mock(QaRetrievalTraceMapper.class);
        QaRetrievalTraceRepositoryImpl repository = new QaRetrievalTraceRepositoryImpl(mapper);
        QaRetrievalTraceDO dataObject = new QaRetrievalTraceDO(
                5001L,
                2001L,
                "黄帝是谁",
                "fastgpt",
                "kb-1",
                "[\"doc-1\"]",
                "chat-1",
                "req-1",
                120L,
                null,
                "{\"answer\":\"黄帝 传说人物\"}",
                9001L,
                "SUCCEEDED",
                null,
                null,
                Instant.now());
        when(mapper.selectOne(any())).thenReturn(dataObject);

        QaRetrievalTrace result = repository.getByMessageId(2001L);

        assertEquals(5001L, result.getId());
        assertEquals("fastgpt", result.getProvider());
        assertEquals(9001L, result.getAiCallId());
        assertEquals("SUCCEEDED", result.getAiStatus());
    }
}
