package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaContextAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.application.search.service.QueryUnderstandingApplicationService;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class QaApplicationServiceImplAdminReadTest {

    @Test
    void readMethodsShouldExposeSessionSourceAndTrace() {
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        QueryUnderstandingApplicationService queryUnderstandingApplicationService =
                mock(QueryUnderstandingApplicationService.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaApplicationServiceImpl service = new QaApplicationServiceImpl(
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                queryUnderstandingApplicationService,
                classicsFacade,
                aiFacade,
                new QaContextAssembler(),
                new QaSourceAssembler(),
                new QaTraceAssembler());

        when(sessionRepository.getBySessionId(5001L))
                .thenReturn(new QaSession(
                        1L,
                        5001L,
                        1001L,
                        "黄帝问答",
                        "GLOBAL",
                        "SEARCH",
                        "SANCAI_ENTRY",
                        10001L,
                        "OPEN",
                        new Date(1_718_000_000_000L),
                        new Date(1_718_000_100_000L),
                        null));
        when(messageRepository.listBySessionId(5001L))
                .thenReturn(List.of(new QaMessage(
                        7001L, 7001L, 5001L, "USER", "黄帝是谁", "SENT", 1, null, new Date(1_718_000_050_000L), null)));
        when(sourceRepository.listByMessageId(7002L))
                .thenReturn(List.of(new QaSource(
                        9001L,
                        9001L,
                        7002L,
                        "SANCAI_ENTRY",
                        1001L,
                        "SANCAI",
                        "黄帝",
                        "卷一",
                        "上古帝王",
                        1,
                        java.math.BigDecimal.ONE,
                        "CITED",
                        new Date(1_718_000_060_000L))));
        when(traceRepository.getByTraceId(8001L)).thenReturn(trace());

        QaSessionDetailResult sessionDetail = service.getSessionDetail(5001L);
        assertEquals(5001L, sessionDetail.getSessionId());
        assertEquals(1, sessionDetail.getMessages().size());
        QaMessageResult message = sessionDetail.getMessages().get(0);
        assertEquals(7001L, message.getMessageId());
        assertEquals("USER", message.getRole());

        var sources = service.listSourcesByMessageId(7002L);
        assertEquals(1, sources.size());
        assertEquals(9001L, sources.get(0).getSourceId());

        QaTraceResult trace = service.getTraceByTraceId(8001L);
        assertNotNull(trace);
        assertEquals("GLOBAL", trace.getScope());

        verify(sessionRepository).getBySessionId(5001L);
        verify(messageRepository).listBySessionId(5001L);
        verify(sourceRepository).listByMessageId(7002L);
        verify(traceRepository).getByTraceId(8001L);
    }

    private QaRetrievalTrace trace() {
        QaRetrievalTrace trace = new QaRetrievalTrace();
        trace.setId(8001L);
        trace.setTraceId(8001L);
        trace.setMessageId(7002L);
        trace.setRawQuestion("黄帝是谁");
        trace.setRewrittenQuestion("黄帝是谁");
        trace.setScope("GLOBAL");
        trace.setFiltersJson("{\"sessionId\":5001}");
        trace.setExpandedTermsJson("[\"轩辕\"]");
        trace.setLinkedEntitiesJson("[{\"name\":\"黄帝\"}]");
        trace.setCandidateCount(1);
        trace.setContextSnapshot("{\"sources\":[]}");
        trace.setRetrievedAt(new Date(1_718_000_070_000L));
        return trace;
    }
}
