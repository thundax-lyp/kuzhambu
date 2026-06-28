package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaContextAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.application.search.query.SearchQuery;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
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

class QaApplicationServiceImplTest {

    @Test
    void openSessionShouldPersistAndReturnSessionResult() {
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
        when(sessionRepository.save(any(QaSession.class))).thenReturn(9001L);

        var result = service.openSession(new OpenQaSessionCommand(
                1001L, "黄帝问答", "GLOBAL", "SEARCH", "SANCAI_ENTRY", 10001L, "req-1", "trace-1"));

        assertEquals(9001L, result.getSessionId());
        assertEquals("黄帝问答", result.getTitle());
        verify(sessionRepository).save(any(QaSession.class));
    }

    @Test
    void askQuestionShouldPersistMessagesSourcesAndTrace() {
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

        QaSession session = new QaSession(
                1L,
                5001L,
                1001L,
                "黄帝问答",
                "GLOBAL",
                "SEARCH",
                "SANCAI_ENTRY",
                10001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);
        when(messageRepository.save(any(QaMessage.class))).thenReturn(7001L, 7002L);
        when(queryUnderstandingApplicationService.understand(any(SearchQuery.class)))
                .thenReturn(new QueryUnderstandingResult(
                        "黄帝是谁",
                        "黄帝是谁",
                        "NATURAL_LANGUAGE_SEARCH",
                        List.of("轩辕"),
                        List.of(new QueryUnderstandingResult.RecognizedEntityResult("黄帝", "PERSON", "黄帝")),
                        "req-1",
                        "trace-1"));
        when(classicsFacade.listPublicContents())
                .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                        .contents(List.of(ClassicsPublicContentFacadeDto.builder()
                                .contentType("SANCAI_ENTRY")
                                .contentId("1001")
                                .knowledgeBase("SANCAI")
                                .categoryCode("11")
                                .categoryName("卷一")
                                .title("黄帝")
                                .summary("上古帝王")
                                .textSegments(List.of("黄帝是上古帝王"))
                                .tagNames(List.of("礼制"))
                                .status("PUBLISHED")
                                .visibility("PUBLIC")
                                .currentVersionNo(1)
                                .publishedAt(new Date())
                                .updatedAt(new Date())
                                .build()))
                        .build());
        when(aiFacade.generateDiscoveryAnswer(any()))
                .thenReturn(DiscoveryAiFacadeResponse.builder()
                        .callId(1L)
                        .status("SUCCEEDED")
                        .capability("answer_generation")
                        .resultFormat("TEXT")
                        .resultPayload("黄帝是上古帝王")
                        .build());
        when(sourceRepository.save(any(QaSource.class))).thenReturn(9001L);
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(8001L);

        QaAnswerResult result =
                service.askQuestion(new AskQuestionCommand(5001L, "黄帝是谁", 1, "USER", "1001", "req-1", "trace-1"));

        assertEquals("SUCCEEDED", result.getAnswerStatus());
        assertEquals("黄帝是上古帝王", result.getAnswer());
        assertEquals(1, result.getSources().size());
        assertEquals(9001L, result.getSources().get(0).getSourceId());
        assertNotNull(result.getTraceSummary());
        verify(messageRepository, times(2)).save(any(QaMessage.class));
        verify(sourceRepository).save(any(QaSource.class));
        verify(traceRepository).save(any(QaRetrievalTrace.class));
        verify(sessionRepository).update(any(QaSession.class));
    }
}
