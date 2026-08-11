package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.DiscoveryAiStreamHandler;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaKnowledgeSyncItem;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageRole;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncItemRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KnowledgeQaApplicationServiceImplTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void chatCompletionShouldRejectRemovedSession() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                sessionRepository,
                mock(QaMessageRepository.class),
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        QaSession session = openSession();
        session.markRemoved(Instant.now());
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(session);

        BizException exception = assertThrows(BizException.class, () -> service.chatCompletion(command()));

        assertEquals("QA_SESSION_ALREADY_REMOVED", exception.getCode());
        verify(knowledgeBaseClient, never()).chat(org.mockito.ArgumentMatchers.any(KnowledgeChatRequest.class));
    }

    @Test
    void chatCompletionShouldInvokeAiFacadeAndPersistAnswerSourceAndTrace() throws Exception {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(wangqiSingleDocumentSession());
        when(messageRepository.save(any(QaMessage.class))).thenReturn(messageId(6001L), messageId(6002L));
        when(aiFacade.generateDiscoveryAnswer(any(DiscoveryAiFacadeRequest.class)))
                .thenReturn(DiscoveryAiFacadeResponse.builder()
                        .callId(9101L)
                        .status("SUCCEEDED")
                        .capability("answer_generation")
                        .resultFormat("JSON")
                        .resultPayload("{\"answer\":\"王圻文档答案\",\"sources\":[],\"finishReason\":\"stop\"}")
                        .build());
        when(sourceRepository.save(any(QaSource.class))).thenReturn(6101L);
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(6201L);
        when(enhancementProvider.enhance("这份文档说了什么？"))
                .thenReturn(new com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult(
                        null, List.of()));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(qaKnowledge());

        service.chatCompletion(wangqiCommand(3001L));

        verify(knowledgeBaseClient, never()).chat(any(KnowledgeChatRequest.class));
        ArgumentCaptor<DiscoveryAiFacadeRequest> aiRequestCaptor =
                ArgumentCaptor.forClass(DiscoveryAiFacadeRequest.class);
        verify(aiFacade).generateDiscoveryAnswer(aiRequestCaptor.capture());
        JsonNode inputPayload =
                OBJECT_MAPPER.readTree(aiRequestCaptor.getValue().getInputPayloadJson());
        assertEquals("这份文档说了什么？", inputPayload.get("question").asText());
        assertEquals(
                "WANGQI_DOCUMENT:3001", inputPayload.at("/sources/0/sourceId").asText());

        ArgumentCaptor<QaMessage> messageCaptor = ArgumentCaptor.forClass(QaMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(messageCaptor.capture());
        assertEquals(role("user"), messageCaptor.getAllValues().get(0).getRole());
        assertEquals(role("assistant"), messageCaptor.getAllValues().get(1).getRole());
        assertEquals("王圻文档答案", messageCaptor.getAllValues().get(1).getContent());
        ArgumentCaptor<QaSource> sourceCaptor = ArgumentCaptor.forClass(QaSource.class);
        verify(sourceRepository).save(sourceCaptor.capture());
        assertEquals("WANGQI_DOCUMENT:3001", sourceCaptor.getValue().getSourceBusinessId());

        ArgumentCaptor<QaRetrievalTrace> traceCaptor = ArgumentCaptor.forClass(QaRetrievalTrace.class);
        verify(traceRepository).save(traceCaptor.capture());
        QaRetrievalTrace trace = traceCaptor.getValue();
        assertEquals(9101L, trace.getAiCallId());
        assertEquals("SUCCEEDED", trace.getAiStatus());
        assertTrue(trace.getRaw().contains("\"aiRequest\""));
        assertTrue(trace.getRaw().contains("\"aiResponse\""));
    }

    @Test
    void chatCompletionStreamShouldStreamSingleDocumentAiDeltas() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(wangqiSingleDocumentSession());
        when(messageRepository.save(any(QaMessage.class))).thenReturn(messageId(6001L), messageId(6002L));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(qaKnowledge());
        when(aiFacade.streamDiscoveryAnswer(any(DiscoveryAiFacadeRequest.class), any(DiscoveryAiStreamHandler.class)))
                .thenAnswer(invocation -> {
                    DiscoveryAiStreamHandler handler = invocation.getArgument(1);
                    handler.onDelta("王圻");
                    handler.onDelta("文档答案");
                    return DiscoveryAiFacadeResponse.builder()
                            .callId(9103L)
                            .status("SUCCEEDED")
                            .capability("answer_generation")
                            .resultFormat("JSON")
                            .resultPayload("{\"answer\":\"王圻文档答案\",\"sources\":[],\"finishReason\":\"stop\"}")
                            .build();
                });
        when(sourceRepository.save(any(QaSource.class))).thenReturn(6101L);
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(6201L);
        when(enhancementProvider.enhance("这份文档说了什么？"))
                .thenReturn(new com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult(
                        null, List.of()));
        List<String> deltas = new java.util.ArrayList<>();

        ChatCompletionResult result = service.chatCompletionStream(wangqiCommand(3001L), deltas::add);

        assertEquals(List.of("王圻", "文档答案"), deltas);
        assertEquals("王圻文档答案", result.getChoices().get(0).getMessage().getContent());
        verify(aiFacade, never()).generateDiscoveryAnswer(any(DiscoveryAiFacadeRequest.class));
        verify(aiFacade)
                .streamDiscoveryAnswer(any(DiscoveryAiFacadeRequest.class), any(DiscoveryAiStreamHandler.class));
        verify(knowledgeBaseClient, never()).chatStream(any(KnowledgeChatRequest.class), any());
    }

    @Test
    void chatCompletionShouldRejectMismatchedSingleDocumentMetadata() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                sessionRepository,
                messageRepository,
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(wangqiSingleDocumentSession());

        BizException exception = assertThrows(BizException.class, () -> service.chatCompletion(wangqiCommand(3002L)));

        assertEquals("DISCOVERY-30013", exception.getCode());
        verify(knowledgeBaseClient, never()).chat(any(KnowledgeChatRequest.class));
        verify(messageRepository, never()).save(any(QaMessage.class));
    }

    @Test
    void chatCompletionShouldPersistFailedAiAnswerAndTrace() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                sessionRepository,
                messageRepository,
                mock(QaSourceRepository.class),
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(wangqiSingleDocumentSession());
        when(messageRepository.save(any(QaMessage.class))).thenReturn(messageId(6001L), messageId(6002L));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(qaKnowledge());
        when(aiFacade.generateDiscoveryAnswer(any(DiscoveryAiFacadeRequest.class)))
                .thenReturn(DiscoveryAiFacadeResponse.builder()
                        .callId(9102L)
                        .status("FAILED")
                        .errorType("WORKER_STREAM")
                        .errorMessage("stream interrupted")
                        .build());
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(6202L);

        service.chatCompletion(wangqiCommand(3001L));

        ArgumentCaptor<QaMessage> messageCaptor = ArgumentCaptor.forClass(QaMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(messageCaptor.capture());
        assertEquals(role("user"), messageCaptor.getAllValues().get(0).getRole());
        assertEquals(role("assistant"), messageCaptor.getAllValues().get(1).getRole());
        assertEquals("FAILED", messageCaptor.getAllValues().get(1).getAnswerStatus());
        assertEquals("stream interrupted", messageCaptor.getAllValues().get(1).getFailureReason());
        ArgumentCaptor<QaRetrievalTrace> traceCaptor = ArgumentCaptor.forClass(QaRetrievalTrace.class);
        verify(traceRepository).save(traceCaptor.capture());
        assertEquals(9102L, traceCaptor.getValue().getAiCallId());
        assertEquals("FAILED", traceCaptor.getValue().getAiStatus());
        assertEquals("WORKER_STREAM", traceCaptor.getValue().getAiErrorType());
        assertEquals("stream interrupted", traceCaptor.getValue().getAiErrorMessage());
    }

    @Test
    void chatCompletionShouldFallbackToLocalRetrievalWhenKnowledgeProviderFails() {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        QaKnowledgeSyncItemRepository syncItemRepository = mock(QaKnowledgeSyncItemRepository.class);
        QaSessionRepository sessionRepository = mock(QaSessionRepository.class);
        QaMessageRepository messageRepository = mock(QaMessageRepository.class);
        QaSourceRepository sourceRepository = mock(QaSourceRepository.class);
        QaRetrievalTraceRepository traceRepository = mock(QaRetrievalTraceRepository.class);
        DiscoveryKnowledgeEnhancementProvider enhancementProvider = mock(DiscoveryKnowledgeEnhancementProvider.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                syncItemRepository,
                sessionRepository,
                messageRepository,
                sourceRepository,
                traceRepository,
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                enhancementProvider);
        when(sessionRepository.getBySessionId(sessionId(5001L))).thenReturn(openSession());
        when(messageRepository.save(any(QaMessage.class))).thenReturn(messageId(6001L), messageId(6002L));
        when(enhancementProvider.enhance("什么是三才？"))
                .thenReturn(new com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult(
                        null, List.of()));
        when(knowledgeBaseClient.chat(any(KnowledgeChatRequest.class)))
                .thenThrow(new IllegalStateException("System not embedding model"));
        when(syncItemRepository.listBySyncStatus(syncStatus("SUCCEEDED"), 20)).thenReturn(List.of(sancaiSyncItem()));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(sancaiKnowledge());
        when(sourceRepository.save(any(QaSource.class))).thenReturn(6103L);
        when(traceRepository.save(any(QaRetrievalTrace.class))).thenReturn(6203L);

        ChatCompletionResult result = service.chatCompletion(command());

        assertEquals("SUCCEEDED", result.getAnswerStatus());
        assertEquals(null, result.getFailureReason());
        ArgumentCaptor<QaMessage> messageCaptor = ArgumentCaptor.forClass(QaMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(messageCaptor.capture());
        assertEquals("什么是三才？", messageCaptor.getAllValues().get(0).getContent());
        QaMessage answerMessage = messageCaptor.getAllValues().get(1);
        assertEquals(role("assistant"), answerMessage.getRole());
        assertTrue(answerMessage.getContent().contains("三才指天地人"));
        assertEquals("SUCCEEDED", answerMessage.getAnswerStatus());
        verify(sourceRepository).save(any(QaSource.class));
        ArgumentCaptor<QaRetrievalTrace> traceCaptor = ArgumentCaptor.forClass(QaRetrievalTrace.class);
        verify(traceRepository).save(traceCaptor.capture());
        assertTrue(traceCaptor.getValue().getFailureReason().contains("Provider failed; answered by local retrieval"));
    }

    @Test
    void buildSingleDocumentAiRequestShouldIncludeKnowledgeRecentMessagesAndSources() throws Exception {
        KnowledgeBaseClient knowledgeBaseClient = mock(KnowledgeBaseClient.class);
        ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
        AiFacade aiFacade = mock(AiFacade.class);
        KnowledgeQaApplicationServiceImpl service = new KnowledgeQaApplicationServiceImpl(
                knowledgeBaseClient,
                classicsFacade,
                aiFacade,
                mock(QaKnowledgeSyncItemRepository.class),
                mock(QaSessionRepository.class),
                mock(QaMessageRepository.class),
                mock(QaSourceRepository.class),
                mock(QaRetrievalTraceRepository.class),
                new QaSourceAssembler(),
                new QaTraceAssembler(),
                mock(DiscoveryKnowledgeEnhancementProvider.class));
        when(classicsFacade.getQaKnowledge(any(ClassicsQaKnowledgeFacadeRequest.class)))
                .thenReturn(qaKnowledge());

        DiscoveryAiFacadeRequest request = service.buildSingleDocumentAiRequest(
                multiTurnWangqiCommand(), wangqiSingleDocumentSession(), "kuzhambu-qa", "当前问题");

        assertEquals("discovery-answer-generation", request.getServiceRole());
        assertEquals("kuzhambu-qa", request.getModelName());
        assertFalse(request.isStream());
        assertTrue(request.isForceJson());
        JsonNode promptMessages = OBJECT_MAPPER.readTree(request.getPromptMessagesJson());
        assertEquals("system", promptMessages.get(0).get("role").asText());
        assertEquals("assistant", promptMessages.get(1).get("role").asText());
        assertEquals("当前问题", promptMessages.get(6).get("content").asText());

        JsonNode inputPayload = OBJECT_MAPPER.readTree(request.getInputPayloadJson());
        assertEquals(5001L, inputPayload.at("/session/sessionId").asLong());
        assertEquals("当前问题", inputPayload.get("question").asText());
        assertEquals("SINGLE_DOCUMENT", inputPayload.at("/context/contextMode").asText());
        assertEquals(
                "WANGQI_DOCUMENT",
                inputPayload.at("/context/contextContentType").asText());
        assertEquals(3001L, inputPayload.at("/context/contextContentId").asLong());
        assertEquals(
                "WANGQI_DOCUMENT:3001", inputPayload.at("/knowledge/sourceId").asText());
        assertEquals("王圻文档内容", inputPayload.at("/knowledge/body").asText());
        assertEquals(6, inputPayload.get("recentMessages").size());
        assertEquals(
                "WANGQI_DOCUMENT:3001", inputPayload.at("/sources/0/sourceId").asText());
        assertEquals(
                "/classics/wangqi/3001",
                inputPayload.at("/sources/0/sourcePath").asText());
    }

    private static ChatCompletionCommand command() {
        return new ChatCompletionCommand(
                5001L, null, List.of(new ChatCompletionMessage("user", "什么是三才？")), false, null, null, null, null);
    }

    private static ChatCompletionCommand wangqiCommand(Long contextContentId) {
        return new ChatCompletionCommand(
                5001L,
                null,
                List.of(new ChatCompletionMessage("user", "这份文档说了什么？")),
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

    private static ChatCompletionCommand multiTurnWangqiCommand() {
        return new ChatCompletionCommand(
                5001L,
                null,
                List.of(
                        new ChatCompletionMessage("user", "第一问"),
                        new ChatCompletionMessage("assistant", "第一答"),
                        new ChatCompletionMessage("user", "第二问"),
                        new ChatCompletionMessage("assistant", "第二答"),
                        new ChatCompletionMessage("user", "第三问"),
                        new ChatCompletionMessage("assistant", "第三答"),
                        new ChatCompletionMessage("user", "当前问题")),
                false,
                Map.of(
                        "contextMode",
                        "SINGLE_DOCUMENT",
                        "contextContentType",
                        "WANGQI_DOCUMENT",
                        "contextContentId",
                        3001L),
                Map.of("temperature", 0.2d),
                "req-qa",
                "trace-qa");
    }

    private static QaSession openSession() {
        return new QaSession(
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
                Instant.now(),
                Instant.now(),
                null);
    }

    private static QaSession wangqiSingleDocumentSession() {
        return new QaSession(
                5001L,
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
                Instant.now(),
                Instant.now(),
                null);
    }

    private static QaKnowledgeSyncItem sancaiSyncItem() {
        return new QaKnowledgeSyncItem(
                1L,
                "SANCAI_ENTRY:3001",
                "SANCAI_ENTRY",
                3001L,
                "kuzhambu-qa",
                1,
                "revision",
                "fastgpt",
                "kb_kuzhambu_qa",
                "item_3001",
                "SUCCEEDED",
                null,
                Instant.now(),
                Instant.now(),
                Instant.now());
    }

    private static ClassicsQaKnowledgeFacadeResponse qaKnowledge() {
        return ClassicsQaKnowledgeFacadeResponse.builder()
                .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                        .sourceId("WANGQI_DOCUMENT:3001")
                        .contentType("WANGQI_DOCUMENT")
                        .contentId("3001")
                        .knowledgeBase("kuzhambu-qa")
                        .currentVersionNo(2)
                        .visibility("PUBLIC")
                        .status("ACTIVE")
                        .sourcePath("/classics/wangqi/3001")
                        .title("王圻文档")
                        .categoryPath("典章")
                        .summary("王圻文档摘要")
                        .body("王圻文档内容")
                        .tags(List.of("礼学"))
                        .qaPairs(List.of(ClassicsQaKnowledgeFacadeDto.QaPair.builder()
                                .question("王圻是谁")
                                .answer("明代学者")
                                .build()))
                        .build())
                .build();
    }

    private static ClassicsQaKnowledgeFacadeResponse sancaiKnowledge() {
        return ClassicsQaKnowledgeFacadeResponse.builder()
                .knowledge(ClassicsQaKnowledgeFacadeDto.builder()
                        .sourceId("SANCAI_ENTRY:3001")
                        .contentType("SANCAI_ENTRY")
                        .contentId("3001")
                        .knowledgeBase("kuzhambu-qa")
                        .currentVersionNo(1)
                        .visibility("PUBLIC")
                        .status("ACTIVE")
                        .sourcePath("/classics/sancai/3001")
                        .title("三才")
                        .categoryPath("术语")
                        .summary("三才指天地人，是传统知识分类中的核心概念。")
                        .body("三才指天地人。")
                        .tags(List.of("三才"))
                        .build())
                .build();
    }

    private static QaSessionId sessionId(Long value) {
        return QaSessionIdCodec.toDomain(value);
    }

    private static QaMessageId messageId(Long value) {
        return QaMessageIdCodec.toDomain(value);
    }

    private static QaKnowledgeSyncStatus syncStatus(String value) {
        return QaStringValueCodec.toKnowledgeSyncStatus(value);
    }

    private static QaMessageRole role(String value) {
        return QaStringValueCodec.toMessageRole(value);
    }
}
