package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.common.knowledge.client.KnowledgeBaseClient;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatChoice;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatMessage;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatRequest;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatResult;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatSource;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ChatCompletionMessage;
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.ChatCompletionStreamHandler;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
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
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaKnowledgeSyncItemRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class KnowledgeQaApplicationServiceImpl implements KnowledgeQaApplicationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_MODEL = "kuzhambu-qa";
    private static final Long DEFAULT_AI_SERVICE_ID = 0L;
    private static final String DEFAULT_AI_SERVICE_ROLE = "discovery-answer-generation";
    private static final Long DEFAULT_AI_MODEL_ID = 0L;
    private static final String DEFAULT_AI_MODEL_NAME = "discovery-default";
    private static final Long DEFAULT_AI_PROMPT_VERSION_ID = 0L;
    private static final String DEFAULT_LOCALE = "zh-CN";
    private static final String MESSAGE_ROLE_USER = "user";
    private static final String MESSAGE_ROLE_ASSISTANT = "assistant";
    private static final String ANSWER_STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String ANSWER_STATUS_FAILED = "FAILED";
    private static final String DEFAULT_FAILURE_REASON = "问答生成失败，请稍后重试。";
    private static final String TRACE_FAILURE_REASON = "Knowledge base chat request failed";
    private static final String DEFAULT_AI_FAILURE_REASON = "Discovery AI answer generation failed";
    private static final String SINGLE_DOCUMENT_CONTEXT_MODE = "SINGLE_DOCUMENT";
    private static final String WANGQI_DOCUMENT_CONTEXT_TYPE = "WANGQI_DOCUMENT";
    private static final String SYNC_STATUS_SUCCEEDED = "SUCCEEDED";
    private static final int LOCAL_RETRIEVAL_LIMIT = 20;
    private static final int LOCAL_RETRIEVAL_RESULT_LIMIT = 3;

    private final KnowledgeBaseClient knowledgeBaseClient;
    private final ClassicsFacade classicsFacade;
    private final AiFacade aiFacade;
    private final QaKnowledgeSyncItemRepository qaKnowledgeSyncItemRepository;
    private final QaSessionRepository qaSessionRepository;
    private final QaMessageRepository qaMessageRepository;
    private final QaSourceRepository qaSourceRepository;
    private final QaRetrievalTraceRepository qaRetrievalTraceRepository;
    private final QaSourceAssembler qaSourceAssembler;
    private final QaTraceAssembler qaTraceAssembler;
    private final DiscoveryKnowledgeEnhancementProvider discoveryKnowledgeEnhancementProvider;

    public KnowledgeQaApplicationServiceImpl(
            KnowledgeBaseClient knowledgeBaseClient,
            ClassicsFacade classicsFacade,
            AiFacade aiFacade,
            QaKnowledgeSyncItemRepository qaKnowledgeSyncItemRepository,
            QaSessionRepository qaSessionRepository,
            QaMessageRepository qaMessageRepository,
            QaSourceRepository qaSourceRepository,
            QaRetrievalTraceRepository qaRetrievalTraceRepository,
            QaSourceAssembler qaSourceAssembler,
            QaTraceAssembler qaTraceAssembler,
            DiscoveryKnowledgeEnhancementProvider discoveryKnowledgeEnhancementProvider) {
        this.knowledgeBaseClient = knowledgeBaseClient;
        this.classicsFacade = classicsFacade;
        this.aiFacade = aiFacade;
        this.qaKnowledgeSyncItemRepository = qaKnowledgeSyncItemRepository;
        this.qaSessionRepository = qaSessionRepository;
        this.qaMessageRepository = qaMessageRepository;
        this.qaSourceRepository = qaSourceRepository;
        this.qaRetrievalTraceRepository = qaRetrievalTraceRepository;
        this.qaSourceAssembler = qaSourceAssembler;
        this.qaTraceAssembler = qaTraceAssembler;
        this.discoveryKnowledgeEnhancementProvider = discoveryKnowledgeEnhancementProvider;
    }

    @Override
    public ChatCompletionResult chatCompletion(ChatCompletionCommand command) {
        validateCommand(command);

        QaSession session = qaSessionRepository.getBySessionId(QaSessionIdCodec.toDomain(command.sessionId()));
        if (session == null) {
            throw new BizException("DISCOVERY-30001", "discovery.qa.session.not-found", "QA session does not exist");
        }
        if (session.isRemoved()) {
            throw new BizException(
                    "QA_SESSION_ALREADY_REMOVED",
                    "discovery.qa.session.already-removed",
                    "QA session has already been removed");
        }
        validateContextMetadata(command, session);

        String model = resolveModel(command, session);
        String question = extractLatestQuestion(command.messages());
        com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement =
                discoveryKnowledgeEnhancementProvider.enhance(question);
        ClassicsQaKnowledgeFacadeDto singleDocumentKnowledge =
                isWangqiSingleDocumentSession(session) ? requireSingleDocumentKnowledge(session) : null;
        DiscoveryAiFacadeRequest aiRequest = singleDocumentKnowledge == null
                ? null
                : buildSingleDocumentAiRequest(command, session, model, question, singleDocumentKnowledge);
        Instant now = Instant.now();
        int contextTurnCount = contextTurnCount(command);

        QaMessage questionMessage = new QaMessage(
                null,
                command.sessionId(),
                MESSAGE_ROLE_USER,
                question,
                "SENT",
                model,
                contextTurnCount,
                null,
                null,
                null,
                now,
                null);
        QaMessageId questionMessagePk = qaMessageRepository.save(questionMessage);
        questionMessage.setId(questionMessagePk);
        if (aiRequest != null) {
            return completeSingleDocumentWithAi(
                    command,
                    session,
                    model,
                    question,
                    contextTurnCount,
                    messageIdValue(questionMessagePk),
                    aiRequest,
                    singleDocumentKnowledge,
                    now);
        }
        KnowledgeChatRequest providerRequest = toKnowledgeChatRequest(command, session, model, question, enhancement);

        KnowledgeChatResult chatResult = null;
        String failureReason = null;
        String providerFailureReason = null;
        try {
            chatResult = knowledgeBaseClient.chat(providerRequest);
        } catch (Exception ex) {
            failureReason = DEFAULT_FAILURE_REASON;
            providerFailureReason = StringUtils.defaultIfBlank(ex.getMessage(), TRACE_FAILURE_REASON);
        } finally {
            session.setLastMessageAt(Instant.now());
            qaSessionRepository.update(session);
        }

        if (chatResult == null) {
            LocalRetrievalAnswer localAnswer = buildLocalRetrievalAnswer(question);
            if (localAnswer != null) {
                QaMessage answerMessage =
                        createLocalRetrievalAnswerMessage(command, model, contextTurnCount, now, localAnswer.answer());
                QaMessageId answerMessagePk = qaMessageRepository.save(answerMessage);
                answerMessage.setId(answerMessagePk);
                answerMessage.setAnsweredAt(Instant.now());
                List<QaSource> sourceEntities =
                        saveLocalRetrievalSources(localAnswer.sources(), messageIdValue(answerMessagePk));
                saveTrace(
                        command,
                        session,
                        answerMessage,
                        question,
                        providerRequest,
                        localRetrievalChatResult(localAnswer, model),
                        now,
                        Instant.now(),
                        "Provider failed; answered by local retrieval: " + providerFailureReason);
                return new ChatCompletionResult(
                        command.sessionId(),
                        messageIdValue(questionMessagePk),
                        messageIdValue(answerMessagePk),
                        question,
                        ANSWER_STATUS_SUCCEEDED,
                        null,
                        List.of(new ChatCompletionResult.ChatCompletionChoice(
                                0,
                                new ChatCompletionResult.ChatCompletionMessage(
                                        MESSAGE_ROLE_ASSISTANT, localAnswer.answer()),
                                "stop")),
                        sourcesToResult(sourceEntities),
                        null,
                        Map.of("fallback", "local-keyword-retrieval"));
            }
            QaMessage failedMessage =
                    createFailureMessage(command.sessionId(), model, contextTurnCount, failureReason, now);
            QaMessageId failedMessagePk = qaMessageRepository.save(failedMessage);
            failedMessage.setId(failedMessagePk);
            saveTrace(
                    command, session, failedMessage, question, providerRequest, null, now, now, providerFailureReason);

            return new ChatCompletionResult(
                    command.sessionId(),
                    messageIdValue(questionMessagePk),
                    messageIdValue(failedMessagePk),
                    question,
                    ANSWER_STATUS_FAILED,
                    failureReason,
                    List.of(),
                    List.of(),
                    null,
                    Map.of());
        }

        List<KnowledgeChatChoice> choices = chatResult.choices() == null ? List.of() : chatResult.choices();
        String answer = resolveAnswer(choices);

        QaMessage answerMessage =
                createAnswerMessage(command, model, contextTurnCount, chatResult, now, answer, choices);
        QaMessageId answerMessagePk = qaMessageRepository.save(answerMessage);
        answerMessage.setId(answerMessagePk);
        answerMessage.setAnsweredAt(Instant.now());

        List<QaSource> sourceEntities =
                qaSourceAssembler.toKnowledgeDomainList(chatResult.sources(), messageIdValue(answerMessagePk));
        for (QaSource sourceEntity : sourceEntities) {
            Long sourcePk = qaSourceRepository.save(sourceEntity);
            sourceEntity.setId(sourcePk);
        }

        saveTrace(command, session, answerMessage, question, providerRequest, chatResult, now, Instant.now(), null);

        return new ChatCompletionResult(
                command.sessionId(),
                messageIdValue(questionMessagePk),
                messageIdValue(answerMessagePk),
                question,
                ANSWER_STATUS_SUCCEEDED,
                null,
                choicesToResult(choices),
                sourcesToResult(sourceEntities),
                toUsageResult(chatResult),
                chatResult.raw());
    }

    @Override
    public ChatCompletionResult chatCompletionStream(
            ChatCompletionCommand command, ChatCompletionStreamHandler streamHandler) {
        validateCommand(command);

        QaSession session = qaSessionRepository.getBySessionId(QaSessionIdCodec.toDomain(command.sessionId()));
        if (session == null) {
            throw new BizException("DISCOVERY-30001", "discovery.qa.session.not-found", "QA session does not exist");
        }
        if (session.isRemoved()) {
            throw new BizException(
                    "QA_SESSION_ALREADY_REMOVED",
                    "discovery.qa.session.already-removed",
                    "QA session has already been removed");
        }
        validateContextMetadata(command, session);

        String model = resolveModel(command, session);
        String question = extractLatestQuestion(command.messages());
        com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement =
                discoveryKnowledgeEnhancementProvider.enhance(question);
        ClassicsQaKnowledgeFacadeDto singleDocumentKnowledge =
                isWangqiSingleDocumentSession(session) ? requireSingleDocumentKnowledge(session) : null;
        DiscoveryAiFacadeRequest aiRequest = singleDocumentKnowledge == null
                ? null
                : buildSingleDocumentAiRequest(command, session, model, question, singleDocumentKnowledge);
        Instant now = Instant.now();
        int contextTurnCount = contextTurnCount(command);

        QaMessage questionMessage = new QaMessage(
                null,
                command.sessionId(),
                MESSAGE_ROLE_USER,
                question,
                "SENT",
                model,
                contextTurnCount,
                null,
                null,
                null,
                now,
                null);
        QaMessageId questionMessagePk = qaMessageRepository.save(questionMessage);
        questionMessage.setId(questionMessagePk);
        if (aiRequest != null) {
            return completeSingleDocumentWithAi(
                    command,
                    session,
                    model,
                    question,
                    contextTurnCount,
                    messageIdValue(questionMessagePk),
                    aiRequest,
                    singleDocumentKnowledge,
                    now,
                    streamHandler);
        }
        KnowledgeChatRequest providerRequest = toKnowledgeChatRequest(command, session, model, question, enhancement);

        KnowledgeChatResult chatResult = null;
        String failureReason = null;
        String providerFailureReason = null;
        try {
            chatResult = knowledgeBaseClient.chatStream(providerRequest, streamHandler::onDelta);
        } catch (Exception ex) {
            providerFailureReason = StringUtils.defaultIfBlank(ex.getMessage(), TRACE_FAILURE_REASON);
            try {
                chatResult = knowledgeBaseClient.chat(toNonStreamRequest(providerRequest));
                String fallbackAnswer = resolveAnswer(chatResult.choices());
                if (StringUtils.isNotBlank(fallbackAnswer)) {
                    streamHandler.onDelta(fallbackAnswer);
                }
            } catch (Exception fallbackEx) {
                failureReason = DEFAULT_FAILURE_REASON;
                providerFailureReason = providerFailureReason + "; non-stream fallback failed: "
                        + StringUtils.defaultIfBlank(fallbackEx.getMessage(), TRACE_FAILURE_REASON);
            }
        } finally {
            session.setLastMessageAt(Instant.now());
            qaSessionRepository.update(session);
        }

        if (chatResult == null) {
            LocalRetrievalAnswer localAnswer = buildLocalRetrievalAnswer(question);
            if (localAnswer != null) {
                if (StringUtils.isNotBlank(localAnswer.answer())) {
                    streamHandler.onDelta(localAnswer.answer());
                }
                QaMessage answerMessage =
                        createLocalRetrievalAnswerMessage(command, model, contextTurnCount, now, localAnswer.answer());
                QaMessageId answerMessagePk = qaMessageRepository.save(answerMessage);
                answerMessage.setId(answerMessagePk);
                answerMessage.setAnsweredAt(Instant.now());
                List<QaSource> sourceEntities =
                        saveLocalRetrievalSources(localAnswer.sources(), messageIdValue(answerMessagePk));
                saveTrace(
                        command,
                        session,
                        answerMessage,
                        question,
                        providerRequest,
                        localRetrievalChatResult(localAnswer, model),
                        now,
                        Instant.now(),
                        "Provider failed; answered by local retrieval: " + providerFailureReason);
                return new ChatCompletionResult(
                        command.sessionId(),
                        messageIdValue(questionMessagePk),
                        messageIdValue(answerMessagePk),
                        question,
                        ANSWER_STATUS_SUCCEEDED,
                        null,
                        List.of(new ChatCompletionResult.ChatCompletionChoice(
                                0,
                                new ChatCompletionResult.ChatCompletionMessage(
                                        MESSAGE_ROLE_ASSISTANT, localAnswer.answer()),
                                "stop")),
                        sourcesToResult(sourceEntities),
                        null,
                        Map.of("fallback", "local-keyword-retrieval"));
            }
            QaMessage failedMessage =
                    createFailureMessage(command.sessionId(), model, contextTurnCount, failureReason, now);
            QaMessageId failedMessagePk = qaMessageRepository.save(failedMessage);
            failedMessage.setId(failedMessagePk);
            saveTrace(
                    command, session, failedMessage, question, providerRequest, null, now, now, providerFailureReason);

            return new ChatCompletionResult(
                    command.sessionId(),
                    messageIdValue(questionMessagePk),
                    messageIdValue(failedMessagePk),
                    question,
                    ANSWER_STATUS_FAILED,
                    failureReason,
                    List.of(),
                    List.of(),
                    null,
                    Map.of());
        }

        List<KnowledgeChatChoice> choices = chatResult.choices() == null ? List.of() : chatResult.choices();
        String answer = resolveAnswer(choices);

        QaMessage answerMessage =
                createAnswerMessage(command, model, contextTurnCount, chatResult, now, answer, choices);
        QaMessageId answerMessagePk = qaMessageRepository.save(answerMessage);
        answerMessage.setId(answerMessagePk);
        answerMessage.setAnsweredAt(Instant.now());

        List<QaSource> sourceEntities =
                qaSourceAssembler.toKnowledgeDomainList(chatResult.sources(), messageIdValue(answerMessagePk));
        for (QaSource sourceEntity : sourceEntities) {
            Long sourcePk = qaSourceRepository.save(sourceEntity);
            sourceEntity.setId(sourcePk);
        }

        saveTrace(command, session, answerMessage, question, providerRequest, chatResult, now, Instant.now(), null);

        return new ChatCompletionResult(
                command.sessionId(),
                messageIdValue(questionMessagePk),
                messageIdValue(answerMessagePk),
                question,
                ANSWER_STATUS_SUCCEEDED,
                null,
                choicesToResult(choices),
                sourcesToResult(sourceEntities),
                toUsageResult(chatResult),
                chatResult.raw());
    }

    private KnowledgeChatRequest toNonStreamRequest(KnowledgeChatRequest request) {
        return new KnowledgeChatRequest(
                request.model(), request.messages(), false, request.metadata(), request.options());
    }

    DiscoveryAiFacadeRequest buildSingleDocumentAiRequest(
            ChatCompletionCommand command, QaSession session, String model, String question) {
        ClassicsQaKnowledgeFacadeDto knowledge = requireSingleDocumentKnowledge(session);
        return buildSingleDocumentAiRequest(command, session, model, question, knowledge);
    }

    private DiscoveryAiFacadeRequest buildSingleDocumentAiRequest(
            ChatCompletionCommand command,
            QaSession session,
            String model,
            String question,
            ClassicsQaKnowledgeFacadeDto knowledge) {
        List<Map<String, Object>> recentMessages = recentMessages(command.messages());
        List<Map<String, Object>> sources = List.of(sourcePayload(knowledge));
        Map<String, Object> context = contextPayload(session);
        DiscoveryAiFacadeRequest request = DiscoveryAiFacadeRequest.builder()
                .serviceId(DEFAULT_AI_SERVICE_ID)
                .serviceRole(DEFAULT_AI_SERVICE_ROLE)
                .modelId(DEFAULT_AI_MODEL_ID)
                .modelName(StringUtils.defaultIfBlank(model, DEFAULT_AI_MODEL_NAME))
                .promptVersionId(DEFAULT_AI_PROMPT_VERSION_ID)
                .requestId(command.requestId())
                .traceId(command.traceId())
                .promptMessagesJson(writeJson(promptMessages(question, recentMessages)))
                .promptVariablesJson(writeJson(Map.of("context", context, "sources", sources)))
                .promptHash(null)
                .inputPayloadJson(
                        writeJson(inputPayload(command, session, question, knowledge, recentMessages, sources)))
                .outputSchemaJson(writeJson(outputSchema()))
                .stream(command.stream())
                .forceJson(true)
                .locale(DEFAULT_LOCALE)
                .build();
        validateAiRequest(request);
        return request;
    }

    private ChatCompletionResult completeSingleDocumentWithAi(
            ChatCompletionCommand command,
            QaSession session,
            String model,
            String question,
            int contextTurnCount,
            Long questionMessagePk,
            DiscoveryAiFacadeRequest aiRequest,
            ClassicsQaKnowledgeFacadeDto knowledge,
            Instant startedAt) {
        return completeSingleDocumentWithAi(
                command,
                session,
                model,
                question,
                contextTurnCount,
                questionMessagePk,
                aiRequest,
                knowledge,
                startedAt,
                null);
    }

    private ChatCompletionResult completeSingleDocumentWithAi(
            ChatCompletionCommand command,
            QaSession session,
            String model,
            String question,
            int contextTurnCount,
            Long questionMessagePk,
            DiscoveryAiFacadeRequest aiRequest,
            ClassicsQaKnowledgeFacadeDto knowledge,
            Instant startedAt,
            ChatCompletionStreamHandler streamHandler) {
        DiscoveryAiFacadeResponse aiResponse = null;
        String failureReason = null;
        try {
            aiResponse = streamHandler == null
                    ? aiFacade.generateDiscoveryAnswer(aiRequest)
                    : aiFacade.streamDiscoveryAnswer(aiRequest, streamHandler::onDelta);
            if (!isAiSucceeded(aiResponse)) {
                failureReason = aiFailureReason(aiResponse);
            }
        } catch (Exception ex) {
            failureReason = StringUtils.defaultIfBlank(ex.getMessage(), DEFAULT_AI_FAILURE_REASON);
        } finally {
            session.setLastMessageAt(Instant.now());
            qaSessionRepository.update(session);
        }

        if (StringUtils.isNotBlank(failureReason)) {
            QaMessage failedMessage =
                    createFailureMessage(command.sessionId(), model, contextTurnCount, failureReason, startedAt);
            QaMessageId failedMessagePk = qaMessageRepository.save(failedMessage);
            failedMessage.setId(failedMessagePk);
            saveAiTrace(
                    command,
                    session,
                    failedMessage,
                    question,
                    aiRequest,
                    aiResponse,
                    List.of(),
                    startedAt,
                    Instant.now(),
                    failureReason);
            return new ChatCompletionResult(
                    command.sessionId(),
                    questionMessagePk,
                    messageIdValue(failedMessagePk),
                    question,
                    ANSWER_STATUS_FAILED,
                    failureReason,
                    List.of(),
                    List.of(),
                    null,
                    aiRaw(aiResponse));
        }

        AiAnswerPayload answerPayload = parseAiAnswerPayload(aiResponse);
        QaMessage answerMessage = createAiAnswerMessage(command, model, contextTurnCount, startedAt, answerPayload);
        QaMessageId answerMessagePk = qaMessageRepository.save(answerMessage);
        answerMessage.setId(answerMessagePk);
        answerMessage.setAnsweredAt(Instant.now());

        QaSource sourceEntity = qaSourceAssembler.toDomain(knowledge, messageIdValue(answerMessagePk), 1);
        Long sourcePk = qaSourceRepository.save(sourceEntity);
        sourceEntity.setId(sourcePk);
        List<QaSource> sourceEntities = List.of(sourceEntity);
        saveAiTrace(
                command,
                session,
                answerMessage,
                question,
                aiRequest,
                aiResponse,
                sourceEntities,
                startedAt,
                Instant.now(),
                null);
        return new ChatCompletionResult(
                command.sessionId(),
                questionMessagePk,
                messageIdValue(answerMessagePk),
                question,
                ANSWER_STATUS_SUCCEEDED,
                null,
                List.of(new ChatCompletionResult.ChatCompletionChoice(
                        0,
                        new ChatCompletionResult.ChatCompletionMessage(MESSAGE_ROLE_ASSISTANT, answerPayload.answer()),
                        answerPayload.finishReason())),
                sourcesToResult(sourceEntities),
                null,
                aiRaw(aiResponse));
    }

    private List<ChatCompletionResult.ChatCompletionSource> sourcesToResult(List<QaSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        List<ChatCompletionResult.ChatCompletionSource> results = new ArrayList<>();
        for (QaSource source : sources) {
            results.add(new ChatCompletionResult.ChatCompletionSource(
                    source.getSourceBusinessId(),
                    source.getKnowledgeBase(),
                    source.getContentType(),
                    source.getContentId() == null ? null : String.valueOf(source.getContentId()),
                    source.getTitleSnapshot(),
                    source.getSnippet(),
                    source.getScore() == null ? null : source.getScore().doubleValue(),
                    source.getSourcePath() == null ? Map.of() : Map.of("sourcePath", source.getSourcePath())));
        }
        return results;
    }

    private LocalRetrievalAnswer buildLocalRetrievalAnswer(String question) {
        if (qaKnowledgeSyncItemRepository == null) {
            return null;
        }
        List<QaKnowledgeSyncItem> syncItems = qaKnowledgeSyncItemRepository.listBySyncStatus(
                QaStringValueCodec.toKnowledgeSyncStatus(SYNC_STATUS_SUCCEEDED), LOCAL_RETRIEVAL_LIMIT);
        if (syncItems == null || syncItems.isEmpty()) {
            return null;
        }
        List<ScoredKnowledge> scoredKnowledge = new ArrayList<>();
        for (QaKnowledgeSyncItem syncItem : syncItems) {
            ClassicsQaKnowledgeFacadeDto knowledge = loadKnowledge(syncItem);
            if (knowledge == null) {
                continue;
            }
            scoredKnowledge.add(new ScoredKnowledge(knowledge, localMatchScore(question, knowledge)));
        }
        if (scoredKnowledge.isEmpty()) {
            return null;
        }
        List<ClassicsQaKnowledgeFacadeDto> matchedSources = scoredKnowledge.stream()
                .filter(item -> item.score() > 0)
                .sorted((left, right) -> Integer.compare(right.score(), left.score()))
                .limit(LOCAL_RETRIEVAL_RESULT_LIMIT)
                .map(ScoredKnowledge::knowledge)
                .toList();
        if (matchedSources.isEmpty()) {
            return new LocalRetrievalAnswer("我在已同步知识库中没有检索到与「" + question + "」直接相关的内容。", List.of());
        }
        return new LocalRetrievalAnswer(localRetrievalAnswerText(question, matchedSources), matchedSources);
    }

    private ClassicsQaKnowledgeFacadeDto loadKnowledge(QaKnowledgeSyncItem syncItem) {
        if (syncItem == null || StringUtils.isBlank(syncItem.getContentType()) || syncItem.getContentId() == null) {
            return null;
        }
        ClassicsQaKnowledgeFacadeResponse response =
                classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                        .contentType(syncItem.getContentType())
                        .contentId(String.valueOf(syncItem.getContentId()))
                        .build());
        return response == null ? null : response.getKnowledge();
    }

    private int localMatchScore(String question, ClassicsQaKnowledgeFacadeDto knowledge) {
        String normalizedQuestion = StringUtils.defaultString(question).trim().toLowerCase();
        String haystack = localSearchText(knowledge).toLowerCase();
        if (StringUtils.isBlank(normalizedQuestion) || StringUtils.isBlank(haystack)) {
            return 0;
        }
        int score = haystack.contains(normalizedQuestion) ? 10 : 0;
        for (String token : localSearchTokens(normalizedQuestion)) {
            if (haystack.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private List<String> localSearchTokens(String normalizedQuestion) {
        String[] parts = normalizedQuestion.split("[\\s,，。！？!?:：;；、]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        if (normalizedQuestion.length() >= 2 && normalizedQuestion.length() <= 24) {
            tokens.add(normalizedQuestion);
        }
        String compactQuestion = normalizedQuestion.replaceAll("[\\s,，。！？!?:：;；、]+", "");
        for (int index = 0; index + 2 <= compactQuestion.length(); index++) {
            tokens.add(compactQuestion.substring(index, index + 2));
        }
        return tokens;
    }

    private String localSearchText(ClassicsQaKnowledgeFacadeDto knowledge) {
        if (knowledge == null) {
            return "";
        }
        List<String> values = new ArrayList<>();
        values.add(knowledge.getTitle());
        values.add(knowledge.getCategoryPath());
        values.add(knowledge.getSummary());
        values.add(knowledge.getBody());
        values.add(knowledge.getOriginalText());
        values.add(knowledge.getTranslationText());
        values.add(knowledge.getOriginalExcerpts());
        if (knowledge.getTags() != null) {
            values.addAll(knowledge.getTags());
        }
        if (knowledge.getQaPairs() != null) {
            for (ClassicsQaKnowledgeFacadeDto.QaPair qaPair : knowledge.getQaPairs()) {
                if (qaPair != null) {
                    values.add(qaPair.getQuestion());
                    values.add(qaPair.getAnswer());
                }
            }
        }
        return values.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
    }

    private String localRetrievalAnswerText(String question, List<ClassicsQaKnowledgeFacadeDto> sources) {
        StringBuilder answer = new StringBuilder();
        answer.append("我在已同步知识库中检索到与「").append(question).append("」相关的内容：");
        for (int index = 0; index < sources.size(); index++) {
            ClassicsQaKnowledgeFacadeDto source = sources.get(index);
            answer.append("\n\n")
                    .append(index + 1)
                    .append(". ")
                    .append(StringUtils.defaultIfBlank(source.getTitle(), source.getSourceId()))
                    .append("：")
                    .append(localSnippet(source));
        }
        return answer.toString();
    }

    private String localSnippet(ClassicsQaKnowledgeFacadeDto knowledge) {
        String snippet = StringUtils.defaultIfBlank(
                knowledge.getSummary(),
                StringUtils.defaultIfBlank(
                        knowledge.getBody(),
                        StringUtils.defaultIfBlank(knowledge.getTranslationText(), knowledge.getOriginalText())));
        if (StringUtils.isBlank(snippet)) {
            return "该来源暂无摘要。";
        }
        String normalized = snippet.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160) + "...";
    }

    private List<QaSource> saveLocalRetrievalSources(List<ClassicsQaKnowledgeFacadeDto> sources, Long messageId) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        List<QaSource> sourceEntities = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            QaSource sourceEntity = qaSourceAssembler.toDomain(sources.get(index), messageId, index + 1);
            Long sourcePk = qaSourceRepository.save(sourceEntity);
            sourceEntity.setId(sourcePk);
            sourceEntities.add(sourceEntity);
        }
        return sourceEntities;
    }

    private KnowledgeChatResult localRetrievalChatResult(LocalRetrievalAnswer answer, String model) {
        return new KnowledgeChatResult(
                "local-keyword-retrieval",
                "chat.completion",
                null,
                model,
                List.of(new KnowledgeChatChoice(
                        0, new KnowledgeChatMessage(MESSAGE_ROLE_ASSISTANT, answer.answer()), "stop")),
                null,
                answer.sources().stream()
                        .map(source -> new KnowledgeChatSource(
                                source.getSourceId(),
                                source.getKnowledgeBase(),
                                source.getContentType(),
                                source.getContentId(),
                                source.getTitle(),
                                localSnippet(source),
                                null,
                                Map.of("sourcePath", StringUtils.defaultString(source.getSourcePath()))))
                        .toList(),
                Map.of("fallback", "local-keyword-retrieval"));
    }

    private ChatCompletionResult.ChatUsageResult toUsageResult(KnowledgeChatResult chatResult) {
        if (chatResult == null || chatResult.usage() == null) {
            return null;
        }
        return new ChatCompletionResult.ChatUsageResult(
                chatResult.usage().promptTokens(),
                chatResult.usage().completionTokens(),
                chatResult.usage().totalTokens());
    }

    private ClassicsQaKnowledgeFacadeDto requireSingleDocumentKnowledge(QaSession session) {
        ClassicsQaKnowledgeFacadeResponse response =
                classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                        .contentType(session.getContextContentType())
                        .contentId(String.valueOf(session.getContextContentId()))
                        .build());
        if (response == null || response.getKnowledge() == null) {
            throw new BizException(
                    "DISCOVERY-30013",
                    "discovery.qa.single-document-context.not-found",
                    "Single document context content does not exist");
        }
        return response.getKnowledge();
    }

    private List<Map<String, Object>> promptMessages(String question, List<Map<String, Object>> recentMessages) {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a Wangqi single-document QA assistant.");
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(recentMessages);
        if (recentMessages.stream()
                .noneMatch(message -> MESSAGE_ROLE_USER.equals(message.get("role"))
                        && StringUtils.equals(question, String.valueOf(message.get("content"))))) {
            Map<String, Object> userMessage = new LinkedHashMap<>();
            userMessage.put("role", MESSAGE_ROLE_USER);
            userMessage.put("content", question);
            messages.add(userMessage);
        }
        return messages;
    }

    private Map<String, Object> inputPayload(
            ChatCompletionCommand command,
            QaSession session,
            String question,
            ClassicsQaKnowledgeFacadeDto knowledge,
            List<Map<String, Object>> recentMessages,
            List<Map<String, Object>> sources) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("session", sessionPayload(session));
        payload.put("question", question);
        payload.put("context", contextPayload(session));
        payload.put("knowledge", knowledgePayload(knowledge));
        payload.put("recentMessages", recentMessages);
        payload.put("sources", sources);
        payload.put("requestId", command.requestId());
        payload.put("traceId", command.traceId());
        return payload;
    }

    private Map<String, Object> sessionPayload(QaSession session) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", session.getSessionId());
        payload.put("title", session.getTitle());
        payload.put("scope", session.getScope());
        return payload;
    }

    private Map<String, Object> contextPayload(QaSession session) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contextMode", session.getContextMode());
        payload.put("contextContentType", session.getContextContentType());
        payload.put("contextContentId", session.getContextContentId());
        return payload;
    }

    private Map<String, Object> knowledgePayload(ClassicsQaKnowledgeFacadeDto knowledge) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceId", knowledge.getSourceId());
        payload.put("contentType", knowledge.getContentType());
        payload.put("contentId", knowledge.getContentId());
        payload.put("knowledgeBase", knowledge.getKnowledgeBase());
        payload.put("currentVersionNo", knowledge.getCurrentVersionNo());
        payload.put("knowledgeRevision", knowledge.getKnowledgeRevision());
        payload.put("visibility", knowledge.getVisibility());
        payload.put("status", knowledge.getStatus());
        payload.put("sourcePath", knowledge.getSourcePath());
        payload.put("title", knowledge.getTitle());
        payload.put("categoryPath", knowledge.getCategoryPath());
        payload.put("summary", knowledge.getSummary());
        payload.put("body", knowledge.getBody());
        payload.put("originalText", knowledge.getOriginalText());
        payload.put("translationText", knowledge.getTranslationText());
        payload.put("originalExcerpts", knowledge.getOriginalExcerpts());
        payload.put("tags", knowledge.getTags() == null ? List.of() : knowledge.getTags());
        payload.put("qaPairs", knowledge.getQaPairs() == null ? List.of() : knowledge.getQaPairs());
        return payload;
    }

    private Map<String, Object> sourcePayload(ClassicsQaKnowledgeFacadeDto knowledge) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceId", knowledge.getSourceId());
        payload.put("knowledgeBase", knowledge.getKnowledgeBase());
        payload.put("contentType", knowledge.getContentType());
        payload.put("contentId", knowledge.getContentId());
        payload.put("title", knowledge.getTitle());
        payload.put("snippet", StringUtils.defaultIfBlank(knowledge.getSummary(), knowledge.getBody()));
        payload.put("sourcePath", knowledge.getSourcePath());
        payload.put("currentVersionNo", knowledge.getCurrentVersionNo());
        return payload;
    }

    private List<Map<String, Object>> recentMessages(List<ChatCompletionMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> mapped = messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> StringUtils.isNotBlank(message.role()) || StringUtils.isNotBlank(message.content()))
                .map(message -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("role", message.role());
                    payload.put("content", message.content());
                    return payload;
                })
                .toList();
        int fromIndex = Math.max(0, mapped.size() - 6);
        return mapped.subList(fromIndex, mapped.size());
    }

    private Map<String, Object> outputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", List.of("answer", "sources"));
        schema.put(
                "properties",
                Map.of(
                        "answer", Map.of("type", "string"),
                        "sources", Map.of("type", "array"),
                        "finishReason", Map.of("type", "string")));
        return schema;
    }

    private String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-30016",
                    "discovery.qa.ai-request-build-failed",
                    "Discovery QA AI request build failed",
                    exception);
        }
    }

    private void validateAiRequest(DiscoveryAiFacadeRequest aiRequest) {
        if (aiRequest == null) {
            return;
        }
        if (StringUtils.isBlank(aiRequest.getPromptMessagesJson())
                || StringUtils.isBlank(aiRequest.getInputPayloadJson())) {
            throw new BizException(
                    "DISCOVERY-30016", "discovery.qa.ai-request-build-failed", "Discovery QA AI request build failed");
        }
    }

    private List<ChatCompletionResult.ChatCompletionChoice> choicesToResult(List<KnowledgeChatChoice> choices) {
        if (choices == null || choices.isEmpty()) {
            return List.of();
        }
        List<ChatCompletionResult.ChatCompletionChoice> mapped = new ArrayList<>();
        for (KnowledgeChatChoice choice : choices) {
            mapped.add(new ChatCompletionResult.ChatCompletionChoice(
                    choice.index(),
                    new ChatCompletionResult.ChatCompletionMessage(
                            choice.message() == null ? null : choice.message().role(),
                            choice.message() == null ? null : choice.message().content()),
                    choice.finishReason()));
        }
        return mapped;
    }

    private void saveTrace(
            ChatCompletionCommand command,
            QaSession session,
            QaMessage answerMessage,
            String question,
            KnowledgeChatRequest providerRequest,
            KnowledgeChatResult chatResult,
            Instant startedAt,
            Instant endAt,
            String failureReason) {
        List<KnowledgeChatSource> sources = chatResult == null ? List.of() : chatResult.sources();
        QaRetrievalTrace trace = qaTraceAssembler.toDomain(
                command,
                session,
                messageIdValue(answerMessage.getId()),
                question,
                providerRequest,
                chatResult,
                sources,
                Math.max(0L, endAt.toEpochMilli() - startedAt.toEpochMilli()),
                failureReason);
        Long tracePk = qaRetrievalTraceRepository.save(trace);
        trace.setId(tracePk);
    }

    private void saveAiTrace(
            ChatCompletionCommand command,
            QaSession session,
            QaMessage answerMessage,
            String question,
            DiscoveryAiFacadeRequest aiRequest,
            DiscoveryAiFacadeResponse aiResponse,
            List<QaSource> sources,
            Instant startedAt,
            Instant endAt,
            String failureReason) {
        QaRetrievalTrace trace = qaTraceAssembler.toAiDomain(
                command,
                session,
                messageIdValue(answerMessage.getId()),
                question,
                aiRequest,
                aiResponse,
                sources,
                Math.max(0L, endAt.toEpochMilli() - startedAt.toEpochMilli()),
                failureReason);
        Long tracePk = qaRetrievalTraceRepository.save(trace);
        trace.setId(tracePk);
    }

    private boolean isAiSucceeded(DiscoveryAiFacadeResponse aiResponse) {
        return aiResponse != null && ANSWER_STATUS_SUCCEEDED.equals(aiResponse.getStatus());
    }

    private String aiFailureReason(DiscoveryAiFacadeResponse aiResponse) {
        if (aiResponse == null) {
            return DEFAULT_AI_FAILURE_REASON;
        }
        return StringUtils.defaultIfBlank(
                aiResponse.getErrorMessage(),
                StringUtils.defaultIfBlank(aiResponse.getErrorType(), DEFAULT_AI_FAILURE_REASON));
    }

    private Map<String, Object> aiRaw(DiscoveryAiFacadeResponse aiResponse) {
        if (aiResponse == null) {
            return Map.of();
        }
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("aiCallId", aiResponse.getCallId());
        raw.put("status", aiResponse.getStatus());
        raw.put("errorType", aiResponse.getErrorType());
        raw.put("errorMessage", aiResponse.getErrorMessage());
        return raw;
    }

    private AiAnswerPayload parseAiAnswerPayload(DiscoveryAiFacadeResponse aiResponse) {
        if (aiResponse == null || StringUtils.isBlank(aiResponse.getResultPayload())) {
            return new AiAnswerPayload(null, null);
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(aiResponse.getResultPayload());
            if (root.isObject()) {
                return new AiAnswerPayload(
                        textOrNull(root.get("answer")),
                        StringUtils.defaultIfBlank(textOrNull(root.get("finishReason")), "stop"));
            }
        } catch (JsonProcessingException exception) {
            return new AiAnswerPayload(aiResponse.getResultPayload(), "stop");
        }
        return new AiAnswerPayload(aiResponse.getResultPayload(), "stop");
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private String resolveAnswer(List<KnowledgeChatChoice> choices) {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        KnowledgeChatChoice firstChoice = choices.get(0);
        if (firstChoice == null || firstChoice.message() == null) {
            return null;
        }
        return StringUtils.defaultString(firstChoice.message().content(), null);
    }

    private String resolveModel(ChatCompletionCommand command, QaSession session) {
        if (StringUtils.isNotBlank(command.model())) {
            return command.model();
        }
        if (session != null && StringUtils.isNotBlank(session.getKnowledgeBaseName())) {
            return session.getKnowledgeBaseName();
        }
        return DEFAULT_MODEL;
    }

    private KnowledgeChatRequest toKnowledgeChatRequest(
            ChatCompletionCommand command,
            QaSession session,
            String model,
            String question,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement) {
        return new KnowledgeChatRequest(
                model,
                toKnowledgeMessages(command.messages()),
                command.stream(),
                enrichedMetadata(command, question, enhancement),
                enrichedOptions(command, session));
    }

    private Map<String, Object> enrichedMetadata(
            ChatCompletionCommand command,
            String question,
            com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (command.metadata() != null) {
            metadata.putAll(command.metadata());
        }
        if (StringUtils.isNotBlank(command.requestId())) {
            metadata.putIfAbsent("requestId", command.requestId());
        }
        if (StringUtils.isNotBlank(command.traceId())) {
            metadata.putIfAbsent("traceId", command.traceId());
        }
        return metadata;
    }

    private Map<String, Object> enrichedOptions(ChatCompletionCommand command, QaSession session) {
        Map<String, Object> options = new LinkedHashMap<>();
        if (command.options() != null) {
            options.putAll(command.options());
        }
        if (isSingleDocumentSession(session)) {
            options.put("contextMode", session.getContextMode());
            options.put("contextContentType", session.getContextContentType());
            options.put("contextContentId", session.getContextContentId());
        }
        return options;
    }

    private List<KnowledgeChatMessage> toKnowledgeMessages(List<ChatCompletionMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .map(message -> new KnowledgeChatMessage(message.role(), message.content()))
                .filter(message -> StringUtils.isNotBlank(message.role()) || StringUtils.isNotBlank(message.content()))
                .collect(Collectors.toList());
    }

    private String extractLatestQuestion(List<ChatCompletionMessage> messages) {
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> MESSAGE_ROLE_USER.equalsIgnoreCase(message.role()))
                .map(ChatCompletionMessage::content)
                .filter(StringUtils::isNotBlank)
                .reduce((first, second) -> second)
                .orElseThrow(() -> new BizException(
                        "DISCOVERY-30003",
                        "discovery.qa.chat-completion.invalid",
                        "Chat completion command is invalid"));
    }

    private QaMessage createAnswerMessage(
            ChatCompletionCommand command,
            String model,
            int contextTurnCount,
            KnowledgeChatResult chatResult,
            Instant sentAt,
            String answer,
            List<KnowledgeChatChoice> choices) {
        return new QaMessage(
                null,
                command.sessionId(),
                MESSAGE_ROLE_ASSISTANT,
                answer,
                ANSWER_STATUS_SUCCEEDED,
                model,
                contextTurnCount,
                null,
                chatResult.id(),
                choices == null || choices.isEmpty() ? null : choices.get(0).finishReason(),
                sentAt,
                null);
    }

    private QaMessage createAiAnswerMessage(
            ChatCompletionCommand command,
            String model,
            int contextTurnCount,
            Instant sentAt,
            AiAnswerPayload answerPayload) {
        return new QaMessage(
                null,
                command.sessionId(),
                MESSAGE_ROLE_ASSISTANT,
                answerPayload.answer(),
                ANSWER_STATUS_SUCCEEDED,
                model,
                contextTurnCount,
                null,
                null,
                answerPayload.finishReason(),
                sentAt,
                null);
    }

    private QaMessage createLocalRetrievalAnswerMessage(
            ChatCompletionCommand command, String model, int contextTurnCount, Instant sentAt, String answer) {
        return new QaMessage(
                null,
                command.sessionId(),
                MESSAGE_ROLE_ASSISTANT,
                answer,
                ANSWER_STATUS_SUCCEEDED,
                model,
                contextTurnCount,
                null,
                "local-keyword-retrieval",
                "stop",
                sentAt,
                null);
    }

    private QaMessage createFailureMessage(
            Long sessionId, String model, int contextTurnCount, String failureReason, Instant sentAt) {
        return new QaMessage(
                null,
                sessionId,
                MESSAGE_ROLE_ASSISTANT,
                "",
                ANSWER_STATUS_FAILED,
                model,
                contextTurnCount,
                failureReason,
                null,
                null,
                sentAt,
                null);
    }

    private int contextTurnCount(ChatCompletionCommand command) {
        return Math.max(0, command.messages().size() - 1);
    }

    private Long messageIdValue(QaMessageId messageId) {
        return QaMessageIdCodec.toValue(messageId);
    }

    private void validateCommand(ChatCompletionCommand command) {
        if (command == null
                || command.sessionId() == null
                || command.messages() == null
                || command.messages().isEmpty()) {
            throw new BizException(
                    "DISCOVERY-30003", "discovery.qa.chat-completion.invalid", "Chat completion command is invalid");
        }
    }

    private void validateContextMetadata(ChatCompletionCommand command, QaSession session) {
        if (!isSingleDocumentSession(session)) {
            return;
        }
        Map<String, Object> metadata = command.metadata() == null ? Map.of() : command.metadata();
        if (!StringUtils.equals(session.getContextMode(), metadataString(metadata.get("contextMode")))
                || !StringUtils.equals(
                        session.getContextContentType(), metadataString(metadata.get("contextContentType")))
                || !Objects.equals(session.getContextContentId(), metadataLong(metadata.get("contextContentId")))) {
            throw new BizException(
                    "DISCOVERY-30013",
                    "discovery.qa.context-metadata.mismatch",
                    "QA chat metadata context does not match session context");
        }
        if (!WANGQI_DOCUMENT_CONTEXT_TYPE.equals(session.getContextContentType())) {
            throw new BizException(
                    "DISCOVERY-30012",
                    "discovery.qa.single-document-context.unsupported",
                    "Single document context only supports WANGQI_DOCUMENT");
        }
    }

    private boolean isSingleDocumentSession(QaSession session) {
        return session != null && SINGLE_DOCUMENT_CONTEXT_MODE.equals(session.getContextMode());
    }

    private boolean isWangqiSingleDocumentSession(QaSession session) {
        return isSingleDocumentSession(session) && WANGQI_DOCUMENT_CONTEXT_TYPE.equals(session.getContextContentType());
    }

    private String metadataString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long metadataLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record AiAnswerPayload(String answer, String finishReason) {}

    private record LocalRetrievalAnswer(String answer, List<ClassicsQaKnowledgeFacadeDto> sources) {}

    private record ScoredKnowledge(ClassicsQaKnowledgeFacadeDto knowledge, int score) {}
}
