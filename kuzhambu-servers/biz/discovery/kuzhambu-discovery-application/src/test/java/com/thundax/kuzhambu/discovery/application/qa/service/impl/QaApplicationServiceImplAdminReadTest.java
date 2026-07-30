package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSessionCsvExporter;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionExportRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaApplicationServiceImplAdminReadTest {

    @Test
    void readMethodsShouldExposeSessionSourceAndTrace() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                mock(QaSessionExportRepository.class),
                mock(ClassicsFacade.class),
                mock(StorageFacade.class),
                new QaSessionCsvExporter(),
                new QaSourceAssembler(),
                new QaTraceAssembler());

        when(sessionRepository.getBySessionId(sessionId(5001L)))
                .thenReturn(new QaSession(
                        5001L,
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
                        Instant.ofEpochMilli(1_718_000_000_000L),
                        Instant.ofEpochMilli(1_718_000_100_000L),
                        null));
        when(messageRepository.listBySessionId(sessionId(5001L)))
                .thenReturn(List.of(new QaMessage(
                        7001L,
                        7001L,
                        5001L,
                        "USER",
                        "黄帝是谁",
                        "SENT",
                        "kuzhambu-qa",
                        1,
                        null,
                        null,
                        null,
                        Instant.ofEpochMilli(1_718_000_050_000L),
                        null)));
        when(sourceRepository.listByMessageId(7002L))
                .thenReturn(List.of(new QaSource(
                        9001L,
                        9001L,
                        "SANCAI_ENTRY:1001",
                        7002L,
                        "SANCAI_ENTRY",
                        1001L,
                        "SANCAI",
                        "黄帝",
                        "卷一",
                        "上古帝王",
                        null,
                        1,
                        java.math.BigDecimal.ONE,
                        "CITED",
                        Instant.ofEpochMilli(1_718_000_060_000L))));
        when(traceRepository.getByTraceId(8001L)).thenReturn(trace());

        QaSessionDetailResult sessionDetail = service.getSessionDetail(5001L);
        assertEquals(5001L, sessionDetail.getId());
        assertEquals(1, sessionDetail.getMessages().size());
        QaMessageResult message = sessionDetail.getMessages().get(0);
        assertEquals(7001L, message.getId());
        assertEquals("USER", message.getRole());

        var sources = service.listSourcesByMessageId(7002L);
        assertEquals(1, sources.size());
        assertEquals(9001L, sources.get(0).getSourceId());

        QaTraceResult trace = service.getTraceByTraceId(8001L);
        assertNotNull(trace);
        assertEquals("黄帝是谁", trace.getRawQuestion());

        verify(sessionRepository).getBySessionId(sessionId(5001L));
        verify(messageRepository).listBySessionId(sessionId(5001L));
        verify(sourceRepository).listByMessageId(7002L);
        verify(traceRepository).getByTraceId(8001L);
    }

    private QaRetrievalTrace trace() {
        QaRetrievalTrace trace = new QaRetrievalTrace();
        trace.setId(8001L);
        trace.setTraceId(8001L);
        trace.setMessageId(7002L);
        trace.setRawQuestion("黄帝是谁");
        trace.setProvider("kuzhambu-qa");
        trace.setExternalKnowledgeBaseId("kb-1");
        trace.setExternalKnowledgeItemIds("[\"item-1\",\"item-2\"]");
        trace.setExternalChatId("chat-1");
        trace.setProviderRequestId("1001");
        trace.setLatencyMs(120L);
        trace.setFailureReason("none");
        trace.setRetrievedAt(Instant.ofEpochMilli(1_718_000_070_000L));
        return trace;
    }

    private QaSessionId sessionId(Long value) {
        return QaSessionIdCodec.toDomain(value);
    }
}
