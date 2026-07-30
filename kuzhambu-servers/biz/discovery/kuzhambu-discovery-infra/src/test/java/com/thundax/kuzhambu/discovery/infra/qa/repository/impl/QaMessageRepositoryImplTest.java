package com.thundax.kuzhambu.discovery.infra.qa.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
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
        doAnswer(invocation -> {
                    invocation.getArgument(0, QaMessageDO.class).setId(2001L);
                    return 1;
                })
                .when(mapper)
                .insert(any(QaMessageDO.class));

        QaMessageId savedId = repository.save(entity);

        assertEquals(2001L, QaMessageIdCodec.toValue(savedId));
        verify(mapper).insert(any(QaMessageDO.class));
    }

    @Test
    void listBySessionIdShouldReturnOrderedMessages() {
        QaMessageMapper mapper = mock(QaMessageMapper.class);
        QaMessageRepositoryImpl repository = new QaMessageRepositoryImpl(mapper);
        QaMessageDO dataObject = new QaMessageDO(
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

        List<QaMessage> result = repository.listBySessionId(QaSessionIdCodec.toDomain(3001L));

        assertEquals(1, result.size());
        assertEquals(2001L, QaMessageIdCodec.toValue(result.get(0).getId()));
        assertEquals("ASSISTANT", result.get(0).getRole());
    }
}
