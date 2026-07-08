package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatChoice;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeQaApplicationServiceImplTest {

    @Test
    void chatCompletionShouldRejectRemovedSession() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                sessionRepository,
                mock(QaMessageRepository.class),
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        QaSession session = openSession();
        session.markRemoved(new Date());
        when(sessionRepository.getBySessionId(5001L)).thenReturn(session);

        BizException exception = assertThrows(BizException.class, () -> service.chatCompletion(command()));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(knowledgeBaseClient, never()).chat(org.mockito.ArgumentMatchers.any(KnowledgeChatRequest.class));
    }

    @Test
    void chatCompletionShouldPassSingleDocumentContextToProviderOptionsAndTrace() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                sessionRepository,
                messageRepository,
                mock(QaSourceRepository.class),
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(wangqiSingleDocumentSession());
        when(messageRepository.save(any(QaMessage.class))).thenReturn(6001L, 6002L);
        when(knowledgeBaseClient.chat(any(KnowledgeChatRequest.class)))
                .thenReturn(new KnowledgeChatResult(
                        "chat-1",
                        "chat.completion",
                        1_718_000_000L,
                        "kuzhambu-qa",
                        List.of(new KnowledgeChatChoice(0, new KnowledgeChatMessage("assistant", "王圻文档答案"), "stop")),
                        null,
                        List.of(),
                        Map.of("id", "chat-1")));
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(6201L);
        when(enhancementProvider.enhance("这份文档说了什么？"))
                .thenReturn(new DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult(
                        List.of("礼学", "典礼"), null, List.of()));

        service.chatCompletion(wangqiCommand(3001L));

        ArgumentCaptor<KnowledgeChatRequest> requestCaptor = ArgumentCaptor.forClass(KnowledgeChatRequest.class);
        verify(knowledgeBaseClient).chat(requestCaptor.capture());
        Map<String, Object> options = requestCaptor.getValue().options();
        assertEquals("SINGLE_DOCUMENT", options.get("contextMode"));
        assertEquals("WANGQI_DOCUMENT", options.get("contextContentType"));
        assertEquals(3001L, options.get("contextContentId"));
        Map<String, Object> metadata = requestCaptor.getValue().metadata();
        assertEquals("这份文档说了什么？", metadata.get("synonymQueryTerm"));
        assertEquals(List.of("礼学", "典礼"), metadata.get("expandedSynonyms"));

        ArgumentCaptor<QaRetrievalTrace> traceCaptor = ArgumentCaptor.forClass(QaRetrievalTrace.class);
        verify(traceRepository).save(traceCaptor.capture());
        String raw = traceCaptor.getValue().getRaw();
        assertTrue(raw.contains("\"providerRequest\""));
        assertTrue(raw.contains("\"contextMode\":\"SINGLE_DOCUMENT\""));
        assertTrue(raw.contains("\"contextContentType\":\"WANGQI_DOCUMENT\""));
        assertTrue(raw.contains("\"contextContentId\":3001"));
        assertTrue(raw.contains("\"providerResponse\""));
    }

    @Test
    void chatCompletionShouldRejectMismatchedSingleDocumentMetadata() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                sessionRepository,
                messageRepository,
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(5001L)).thenReturn(wangqiSingleDocumentSession());

        BizException exception = assertThrows(BizException.class, () -> service.chatCompletion(wangqiCommand(3002L)));

        assertEquals("DISCOVERY-30013", exception.getCode());
        verify(knowledgeBaseClient, never()).chat(any(KnowledgeChatRequest.class));
        verify(messageRepository, never()).save(any(QaMessage.class));
    }

    private static ChatCompletionCommand command() {
        return new ChatCompletionCommand(
                5001L,
                null,
                List.of(new ChatCompletionCommand.ChatMessage("user", "什么是三才？")),
                false,
                null,
                null,
                null,
                null);
    }

    private static ChatCompletionCommand wangqiCommand(Long contextContentId) {
        return new ChatCompletionCommand(
                5001L,
                null,
                List.of(new ChatCompletionCommand.ChatMessage("user", "这份文档说了什么？")),
                false,
                Map.of(
                        "contextMode",
                        "SINGLE_DOCUMENT",
                        "contextContentType",
                        "WANGQI_DOCUMENT",
                        "contextContentId",
                        contextContentId),
                Map.of("temperature", 0.2d),
                null,
                null);
    }

    private static QaSession openSession() {
        return new QaSession(
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
    }

    private static QaSession wangqiSingleDocumentSession() {
        return new QaSession(
                1L,
                5001L,
                "USER",
                "1001",
                "kuzhambu-qa",
                "王圻文档问答",
                "PORTAL",
                "SINGLE_DOCUMENT",
                "WANGQI_DOCUMENT",
                3001L,
                "OPEN",
                new Date(),
                new Date(),
                null);
    }
}
