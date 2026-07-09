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
import com.thundax.kuzhambu.discovery.application.qa.result.ChatCompletionResult;
import com.thundax.kuzhambu.discovery.application.qa.service.KnowledgeQaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.application.search.support.DiscoveryKnowledgeEnhancementProvider;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.ArrayList;
import java.util.Date;
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
    private static final String DEFAULT_FAILURE_REASON = "Knowledge base chat request failed";
    private static final String DEFAULT_AI_FAILURE_REASON = "Discovery AI answer generation failed";
    private static final String SINGLE_DOCUMENT_CONTEXT_MODE = "SINGLE_DOCUMENT";
    private static final String WANGQI_DOCUMENT_CONTEXT_TYPE = "WANGQI_DOCUMENT";

    private final KnowledgeBaseClient knowledgeBaseClient;
    private final ClassicsFacade classicsFacade;
    private final AiFacade aiFacade;
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

        QaSession session = qaSessionRepository.getBySessionId(command.getSessionId());
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
        String question = extractLatestQuestion(command.getMessages());
        DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult enhancement =
                discoveryKnowledgeEnhancementProvider.enhance(question);
        ClassicsQaKnowledgeFacadeDto singleDocumentKnowledge =
                isWangqiSingleDocumentSession(session) ? requireSingleDocumentKnowledge(session) : null;
        DiscoveryAiFacadeRequest aiRequest = singleDocumentKnowledge == null
                ? null
                : buildSingleDocumentAiRequest(command, session, model, question, singleDocumentKnowledge);
        Date now = new Date();
        int contextTurnCount = contextTurnCount(command);

        QaMessage questionMessage = new QaMessage(
                null,
                null,
                command.getSessionId(),
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
        Long questionMessagePk = qaMessageRepository.save(questionMessage);
        questionMessage.setId(questionMessagePk);
        questionMessage.setMessageId(questionMessagePk);
        if (aiRequest != null) {
            return completeSingleDocumentWithAi(
                    command,
                    session,
                    model,
                    question,
                    contextTurnCount,
                    questionMessagePk,
                    aiRequest,
                    singleDocumentKnowledge,
                    now);
        }
        KnowledgeChatRequest providerRequest = toKnowledgeChatRequest(command, session, model, question, enhancement);

        KnowledgeChatResult chatResult = null;
        String failureReason = null;
        try {
            chatResult = knowledgeBaseClient.chat(providerRequest);
        } catch (Exception ex) {
            failureReason = StringUtils.defaultIfBlank(ex.getMessage(), DEFAULT_FAILURE_REASON);
        } finally {
            session.setLastMessageAt(new Date());
            qaSessionRepository.update(session);
        }

        if (chatResult == null) {
            QaMessage failedMessage =
                    createFailureMessage(command.getSessionId(), model, contextTurnCount, failureReason, now);
            Long failedMessagePk = qaMessageRepository.save(failedMessage);
            failedMessage.setId(failedMessagePk);
            failedMessage.setMessageId(failedMessagePk);
            saveTrace(command, session, failedMessage, question, providerRequest, null, now, now, failureReason);

            return new ChatCompletionResult(
                    command.getSessionId(),
                    questionMessagePk,
                    failedMessagePk,
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
        Long answerMessagePk = qaMessageRepository.save(answerMessage);
        answerMessage.setId(answerMessagePk);
        answerMessage.setMessageId(answerMessagePk);
        answerMessage.setAnsweredAt(new Date());

        List<QaSource> sourceEntities = qaSourceAssembler.toKnowledgeDomainList(chatResult.sources(), answerMessagePk);
        for (QaSource sourceEntity : sourceEntities) {
            Long sourcePk = qaSourceRepository.save(sourceEntity);
            sourceEntity.setId(sourcePk);
            if (sourceEntity.getSourceId() == null) {
                sourceEntity.setSourceId(sourcePk);
            }
        }

        saveTrace(command, session, answerMessage, question, providerRequest, chatResult, now, new Date(), null);

        return new ChatCompletionResult(
                command.getSessionId(),
                questionMessagePk,
                answerMessagePk,
                question,
                ANSWER_STATUS_SUCCEEDED,
                null,
                choicesToResult(choices),
                sourcesToResult(sourceEntities),
                toUsageResult(chatResult),
                chatResult.raw());
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
        List<Map<String, Object>> recentMessages = recentMessages(command.getMessages());
        List<Map<String, Object>> sources = List.of(sourcePayload(knowledge));
        Map<String, Object> context = contextPayload(session);
        DiscoveryAiFacadeRequest request = DiscoveryAiFacadeRequest.builder()
                .serviceId(DEFAULT_AI_SERVICE_ID)
                .serviceRole(DEFAULT_AI_SERVICE_ROLE)
                .modelId(DEFAULT_AI_MODEL_ID)
                .modelName(StringUtils.defaultIfBlank(model, DEFAULT_AI_MODEL_NAME))
                .promptVersionId(DEFAULT_AI_PROMPT_VERSION_ID)
                .requestId(command.getRequestId())
                .traceId(command.getTraceId())
                .promptMessagesJson(writeJson(promptMessages(question, recentMessages)))
                .promptVariablesJson(writeJson(Map.of("context", context, "sources", sources)))
                .promptHash(null)
                .inputPayloadJson(
                        writeJson(inputPayload(command, session, question, knowledge, recentMessages, sources)))
                .outputSchemaJson(writeJson(outputSchema()))
                .stream(command.isStream())
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
            Date startedAt) {
        DiscoveryAiFacadeResponse aiResponse = null;
        String failureReason = null;
        try {
            aiResponse = aiFacade.generateDiscoveryAnswer(aiRequest);
            if (!isAiSucceeded(aiResponse)) {
                failureReason = aiFailureReason(aiResponse);
            }
        } catch (Exception ex) {
            failureReason = StringUtils.defaultIfBlank(ex.getMessage(), DEFAULT_AI_FAILURE_REASON);
        } finally {
            session.setLastMessageAt(new Date());
            qaSessionRepository.update(session);
        }

        if (StringUtils.isNotBlank(failureReason)) {
            QaMessage failedMessage =
                    createFailureMessage(command.getSessionId(), model, contextTurnCount, failureReason, startedAt);
            Long failedMessagePk = qaMessageRepository.save(failedMessage);
            failedMessage.setId(failedMessagePk);
            failedMessage.setMessageId(failedMessagePk);
            saveAiTrace(
                    command,
                    session,
                    failedMessage,
                    question,
                    aiRequest,
                    aiResponse,
                    List.of(),
                    startedAt,
                    new Date(),
                    failureReason);
            return new ChatCompletionResult(
                    command.getSessionId(),
                    questionMessagePk,
                    failedMessagePk,
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
        Long answerMessagePk = qaMessageRepository.save(answerMessage);
        answerMessage.setId(answerMessagePk);
        answerMessage.setMessageId(answerMessagePk);
        answerMessage.setAnsweredAt(new Date());

        QaSource sourceEntity = qaSourceAssembler.toDomain(knowledge, answerMessagePk, 1);
        Long sourcePk = qaSourceRepository.save(sourceEntity);
        sourceEntity.setId(sourcePk);
        if (sourceEntity.getSourceId() == null) {
            sourceEntity.setSourceId(sourcePk);
        }
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
                new Date(),
                null);
        return new ChatCompletionResult(
                command.getSessionId(),
                questionMessagePk,
                answerMessagePk,
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
        payload.put("requestId", command.getRequestId());
        payload.put("traceId", command.getTraceId());
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

    private List<Map<String, Object>> recentMessages(List<ChatCompletionCommand.ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> mapped = messages.stream()
                .filter(Objects::nonNull)
                .filter(message ->
                        StringUtils.isNotBlank(message.getRole()) || StringUtils.isNotBlank(message.getContent()))
                .map(message -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("role", message.getRole());
                    payload.put("content", message.getContent());
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
            Date startedAt,
            Date endAt,
            String failureReason) {
        List<KnowledgeChatSource> sources = chatResult == null ? List.of() : chatResult.sources();
        QaRetrievalTrace trace = qaTraceAssembler.toDomain(
                command,
                session,
                answerMessage.getMessageId(),
                question,
                providerRequest,
                chatResult,
                sources,
                Math.max(0L, endAt.getTime() - startedAt.getTime()),
                failureReason);
        Long tracePk = qaRetrievalTraceRepository.save(trace);
        trace.setId(tracePk);
        trace.setTraceId(tracePk);
    }

    private void saveAiTrace(
            ChatCompletionCommand command,
            QaSession session,
            QaMessage answerMessage,
            String question,
            DiscoveryAiFacadeRequest aiRequest,
            DiscoveryAiFacadeResponse aiResponse,
            List<QaSource> sources,
            Date startedAt,
            Date endAt,
            String failureReason) {
        QaRetrievalTrace trace = qaTraceAssembler.toAiDomain(
                command,
                session,
                answerMessage.getMessageId(),
                question,
                aiRequest,
                aiResponse,
                sources,
                Math.max(0L, endAt.getTime() - startedAt.getTime()),
                failureReason);
        Long tracePk = qaRetrievalTraceRepository.save(trace);
        trace.setId(tracePk);
        trace.setTraceId(tracePk);
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
        if (StringUtils.isNotBlank(command.getModel())) {
            return command.getModel();
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
            DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult enhancement) {
        return new KnowledgeChatRequest(
                model,
                toKnowledgeMessages(command.getMessages()),
                command.isStream(),
                enrichedMetadata(command, question, enhancement),
                enrichedOptions(command, session));
    }

    private Map<String, Object> enrichedMetadata(
            ChatCompletionCommand command,
            String question,
            DiscoveryKnowledgeEnhancementProvider.KnowledgeEnhancementResult enhancement) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (command.getMetadata() != null) {
            metadata.putAll(command.getMetadata());
        }
        if (StringUtils.isNotBlank(question)) {
            metadata.put("synonymQueryTerm", question);
        }
        if (enhancement != null
                && enhancement.expandedSynonyms() != null
                && !enhancement.expandedSynonyms().isEmpty()) {
            metadata.put("expandedSynonyms", enhancement.expandedSynonyms());
        }
        if (StringUtils.isNotBlank(command.getRequestId())) {
            metadata.putIfAbsent("requestId", command.getRequestId());
        }
        if (StringUtils.isNotBlank(command.getTraceId())) {
            metadata.putIfAbsent("traceId", command.getTraceId());
        }
        return metadata;
    }

    private Map<String, Object> enrichedOptions(ChatCompletionCommand command, QaSession session) {
        Map<String, Object> options = new LinkedHashMap<>();
        if (command.getOptions() != null) {
            options.putAll(command.getOptions());
        }
        if (isSingleDocumentSession(session)) {
            options.put("contextMode", session.getContextMode());
            options.put("contextContentType", session.getContextContentType());
            options.put("contextContentId", session.getContextContentId());
        }
        return options;
    }

    private List<KnowledgeChatMessage> toKnowledgeMessages(List<ChatCompletionCommand.ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        return messages.stream()
                .filter(Objects::nonNull)
                .map(message -> new KnowledgeChatMessage(message.getRole(), message.getContent()))
                .filter(message -> StringUtils.isNotBlank(message.role()) || StringUtils.isNotBlank(message.content()))
                .collect(Collectors.toList());
    }

    private String extractLatestQuestion(List<ChatCompletionCommand.ChatMessage> messages) {
        return messages.stream()
                .filter(Objects::nonNull)
                .filter(message -> MESSAGE_ROLE_USER.equalsIgnoreCase(message.getRole()))
                .map(ChatCompletionCommand.ChatMessage::getContent)
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
            Date sentAt,
            String answer,
            List<KnowledgeChatChoice> choices) {
        return new QaMessage(
                null,
                null,
                command.getSessionId(),
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
            Date sentAt,
            AiAnswerPayload answerPayload) {
        return new QaMessage(
                null,
                null,
                command.getSessionId(),
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

    private QaMessage createFailureMessage(
            Long sessionId, String model, int contextTurnCount, String failureReason, Date sentAt) {
        return new QaMessage(
                null,
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
        return Math.max(0, command.getMessages().size() - 1);
    }

    private void validateCommand(ChatCompletionCommand command) {
        if (command == null
                || command.getSessionId() == null
                || command.getMessages() == null
                || command.getMessages().isEmpty()) {
            throw new BizException(
                    "DISCOVERY-30003", "discovery.qa.chat-completion.invalid", "Chat completion command is invalid");
        }
    }

    private void validateContextMetadata(ChatCompletionCommand command, QaSession session) {
        if (!isSingleDocumentSession(session)) {
            return;
        }
        Map<String, Object> metadata = command.getMetadata() == null ? Map.of() : command.getMetadata();
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
}
