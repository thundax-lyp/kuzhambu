package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaApplicationServiceImplTest {

    @Test
    void openSessionShouldPersistAndReturnSessionResult() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler());
        when(sessionRepository.save(any(QaSession.class))).thenReturn(9001L);

        var result = service.openSession(new OpenQaSessionCommand(
                1001L, "黄帝问答", "GLOBAL", "SEARCH", "SANCAI_ENTRY", 10001L, "req-1", "trace-1"));

        assertEquals(9001L, result.getSessionId());
        assertEquals("黄帝问答", result.getTitle());
        assertEquals(1001L, result.getOwnerUserId());
        verify(sessionRepository).save(any(QaSession.class));
    }

    @Test
    void getSessionDetailShouldAssembleMessages() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler());

        QaSession session = new QaSession(
                1L,
                5001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "黄帝问答",
                "GLOBAL",
                "GENERAL",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);
        when(messageRepository.listBySessionId(5001L))
                .thenReturn(List.of(new QaMessage(
                        1L, 1L, 5001L, "USER", "黄帝是谁", "SENT", "kuzhambu-qa", 0, null, null, null, new Date(), null)));
        when(sourceRepository.listByMessageId(any())).thenReturn(List.of());
        when(traceRepository.getByTraceId(any())).thenReturn(new QaRetrievalTrace());

        QaSessionDetailResult result = service.getSessionDetail(5001L);

        assertEquals(5001L, result.getSessionId());
        assertEquals("黄帝问答", result.getTitle());
        assertEquals(1, result.getMessages().size());
        verify(messageRepository).listBySessionId(5001L);
    }
}
