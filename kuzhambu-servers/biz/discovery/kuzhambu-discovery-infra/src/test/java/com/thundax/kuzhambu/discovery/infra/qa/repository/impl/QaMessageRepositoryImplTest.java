package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject.QaMessageDO;
import com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper.QaMessageMapper;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaMessageRepositoryImplTest {

    @Test
    void saveShouldAssignIdentifiersWhenMessageIdIsMissing() {
        QaMessageMapper mapper = mock(QaMessageMapper.class);
        QaMessageRepositoryImpl repository = new QaMessageRepositoryImpl(mapper);
        QaMessage entity = new QaMessage(
                null, null, 3001L, "USER", "黄帝是谁", "SENT", "kuzhambu-qa", 0, null, null, null, new Date(), null);

        Long savedId = repository.save(entity);

        assertNotNull(savedId);
        verify(mapper).insert(any(QaMessageDO.class));
    }

    @Test
    void listBySessionIdShouldReturnOrderedMessages() {
        QaMessageMapper mapper = mock(QaMessageMapper.class);
        QaMessageRepositoryImpl repository = new QaMessageRepositoryImpl(mapper);
        QaMessageDO dataObject = new QaMessageDO(
                1L,
                2001L,
                3001L,
                "ASSISTANT",
                "黄帝是上古传说人物",
                "ANSWERED",
                "kuzhambu-qa",
                1,
                null,
                null,
                null,
                new Date(),
                new Date());
        when(mapper.selectList(any())).thenReturn(List.of(dataObject));

        List<QaMessage> result = repository.listBySessionId(3001L);

        assertEquals(1, result.size());
        assertEquals(2001L, result.get(0).getMessageId());
        assertEquals("ASSISTANT", result.get(0).getRole());
    }
}
