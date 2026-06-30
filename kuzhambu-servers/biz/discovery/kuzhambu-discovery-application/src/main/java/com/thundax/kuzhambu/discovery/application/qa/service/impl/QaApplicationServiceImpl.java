package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.DiscoveryAiFacadeRequest;
import com.thundax.kuzhambu.ai.facade.response.DiscoveryAiFacadeResponse;
import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class QaApplicationServiceImpl implements QaApplicationService {

    private static final Long DEFAULT_SERVICE_ID = 0L;
    private static final String DEFAULT_SERVICE_ROLE = "discovery-qa";
    private static final Long DEFAULT_MODEL_ID = 0L;
    private static final String DEFAULT_MODEL_NAME = "discovery-default";
    private static final Long DEFAULT_PROMPT_VERSION_ID = 0L;
    private static final String DEFAULT_LOCALE = "zh-CN";

    private final QaSessionRepository qaSessionRepository;
    private final QaMessageRepository qaMessageRepository;
    private final QaSourceRepository qaSourceRepository;
    private final QaRetrievalTraceRepository qaRetrievalTraceRepository;
    private final QueryUnderstandingApplicationService queryUnderstandingApplicationService;
    private final ClassicsFacade classicsFacade;
    private final AiFacade aiFacade;
    private final QaContextAssembler qaContextAssembler;
    private final QaSourceAssembler qaSourceAssembler;
    private final QaTraceAssembler qaTraceAssembler;

    public QaApplicationServiceImpl(
            QaSessionRepository qaSessionRepository,
            QaMessageRepository qaMessageRepository,
            QaSourceRepository qaSourceRepository,
            QaRetrievalTraceRepository qaRetrievalTraceRepository,
            QueryUnderstandingApplicationService queryUnderstandingApplicationService,
            ClassicsFacade classicsFacade,
            AiFacade aiFacade,
            QaContextAssembler qaContextAssembler,
            QaSourceAssembler qaSourceAssembler,
            QaTraceAssembler qaTraceAssembler) {
        this.qaSessionRepository = qaSessionRepository;
        this.qaMessageRepository = qaMessageRepository;
        this.qaSourceRepository = qaSourceRepository;
        this.qaRetrievalTraceRepository = qaRetrievalTraceRepository;
        this.queryUnderstandingApplicationService = queryUnderstandingApplicationService;
        this.classicsFacade = classicsFacade;
        this.aiFacade = aiFacade;
        this.qaContextAssembler = qaContextAssembler;
        this.qaSourceAssembler = qaSourceAssembler;
        this.qaTraceAssembler = qaTraceAssembler;
    }

    @Override
    public QaSessionResult openSession(OpenQaSessionCommand command) {
        validateOpenSessionCommand(command);
        Date now = new Date();
        QaSession session = new QaSession(
                null,
                null,
                command.getOwnerUserId(),
                StringUtils.defaultIfBlank(command.getTitle(), "问答会话"),
                StringUtils.defaultIfBlank(command.getScope(), "GLOBAL"),
                StringUtils.defaultIfBlank(command.getContextMode(), "GENERAL"),
                command.getContextContentType(),
                command.getContextContentId(),
                "OPEN",
                now,
                now,
                null);
        Long sessionPk = qaSessionRepository.save(session);
        session.setId(sessionPk);
        session.setSessionId(sessionPk);
        return toSessionResult(session);
    }

    @Override
    public QaAnswerResult askQuestion(AskQuestionCommand command) {
        validateAskQuestionCommand(command);
        QaSession session = qaSessionRepository.getBySessionId(command.getSessionId());
        if (session == null) {
            throw new BizException("DISCOVERY-30001", "discovery.qa.session.not-found", "QA session does not exist");
        }

        Date now = new Date();
        QaMessage userMessage = new QaMessage(
                null,
                null,
                command.getSessionId(),
                "USER",
                command.getQuestion(),
                "SENT",
                command.getContextTurnCount() == null ? 0 : command.getContextTurnCount(),
                null,
                now,
                null);
        Long questionMessagePk = qaMessageRepository.save(userMessage);
        userMessage.setId(questionMessagePk);
        userMessage.setMessageId(questionMessagePk);

        SearchQuery understandingQuery = new SearchQuery(
                command.getQuestion(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null,
                1,
                20,
                command.getOperatorType(),
                command.getOperatorId(),
                command.getRequestId(),
                command.getTraceId());
        QueryUnderstandingResult understandingResult =
                queryUnderstandingApplicationService.understand(understandingQuery);
        List<ClassicsPublicContentFacadeDto> publicContents =
                classicsFacade.listPublicContents().getContents();
        QaContextAssembler.QaContext qaContext =
                qaContextAssembler.assemble(command.getQuestion(), understandingResult, publicContents);
        DiscoveryAiFacadeResponse aiResult = aiFacade.generateDiscoveryAnswer(toAiRequest(command, qaContext));
        boolean aiSucceeded = aiResult != null && "SUCCEEDED".equalsIgnoreCase(aiResult.getStatus());

        String answerText = aiResult == null ? null : aiResult.getResultPayload();
        if (StringUtils.isBlank(answerText)) {
            answerText = "暂时没有生成可用回答。";
        }
        String failureReason =
                aiSucceeded ? null : aiResult == null ? "Discovery AI result is missing" : aiResult.getErrorMessage();

        QaMessage assistantMessage = new QaMessage(
                null,
                null,
                command.getSessionId(),
                "ASSISTANT",
                answerText,
                aiSucceeded ? "ANSWERED" : "FAILED",
                command.getContextTurnCount() == null ? 0 : command.getContextTurnCount(),
                failureReason,
                now,
                now);
        Long answerMessagePk = qaMessageRepository.save(assistantMessage);
        assistantMessage.setId(answerMessagePk);
        assistantMessage.setMessageId(answerMessagePk);

        List<QaSource> sourceEntities = new ArrayList<>();
        for (QaSource sourceEntity : qaSourceAssembler.toDomainList(qaContext.sourceContents(), answerMessagePk)) {
            Long sourcePk = qaSourceRepository.save(sourceEntity);
            sourceEntity.setId(sourcePk);
            if (sourceEntity.getSourceId() == null) {
                sourceEntity.setSourceId(sourcePk);
            }
            sourceEntities.add(sourceEntity);
        }

        QaRetrievalTrace traceEntity = qaTraceAssembler.toDomain(
                command,
                session,
                understandingResult,
                qaContext,
                answerMessagePk,
                aiResult == null ? null : aiResult.getCallId());
        Long tracePk = qaRetrievalTraceRepository.save(traceEntity);
        traceEntity.setId(tracePk);
        traceEntity.setTraceId(tracePk);

        session.setLastMessageAt(now);
        qaSessionRepository.update(session);

        return new QaAnswerResult(
                command.getSessionId(),
                questionMessagePk,
                answerMessagePk,
                command.getQuestion(),
                answerText,
                aiSucceeded ? "SUCCEEDED" : "FAILED",
                failureReason,
                qaSourceAssembler.toResultList(sourceEntities),
                qaTraceAssembler.toTraceSummary(traceEntity, understandingResult, qaContext));
    }

    @Override
    public QaSessionDetailResult getSessionDetail(Long sessionId) {
        if (sessionId == null) {
            throw new BizException("DISCOVERY-30006", "discovery.qa.session-id.required", "Session id is required");
        }
        QaSession session = qaSessionRepository.getBySessionId(sessionId);
        if (session == null) {
            throw new BizException("DISCOVERY-30001", "discovery.qa.session.not-found", "QA session does not exist");
        }
        QaSessionDetailResult result = new QaSessionDetailResult();
        result.setSessionId(session.getSessionId());
        result.setOwnerUserId(session.getOwnerUserId());
        result.setTitle(session.getTitle());
        result.setScope(session.getScope());
        result.setContextMode(session.getContextMode());
        result.setContextContentType(session.getContextContentType());
        result.setContextContentId(session.getContextContentId());
        result.setStatus(session.getStatus());
        result.setOpenedAt(
                session.getOpenedAt() == null ? null : session.getOpenedAt().getTime());
        result.setLastMessageAt(
                session.getLastMessageAt() == null
                        ? null
                        : session.getLastMessageAt().getTime());
        result.setMessages(qaMessageRepository.listBySessionId(sessionId).stream()
                .map(this::toMessageResult)
                .toList());
        return result;
    }

    @Override
    public List<QaSourceResult> listSourcesByMessageId(Long messageId) {
        if (messageId == null) {
            throw new BizException("DISCOVERY-30007", "discovery.qa.message-id.required", "Message id is required");
        }
        return qaSourceAssembler.toResultList(qaSourceRepository.listByMessageId(messageId));
    }

    @Override
    public QaTraceResult getTraceByTraceId(Long traceId) {
        if (traceId == null) {
            throw new BizException("DISCOVERY-30008", "discovery.qa.trace-id.required", "Trace id is required");
        }
        return qaTraceAssembler.toTraceResult(qaRetrievalTraceRepository.getByTraceId(traceId));
    }

    private DiscoveryAiFacadeRequest toAiRequest(AskQuestionCommand command, QaContextAssembler.QaContext qaContext) {
        return DiscoveryAiFacadeRequest.builder()
                .serviceId(DEFAULT_SERVICE_ID)
                .serviceRole(DEFAULT_SERVICE_ROLE)
                .modelId(DEFAULT_MODEL_ID)
                .modelName(DEFAULT_MODEL_NAME)
                .promptVersionId(DEFAULT_PROMPT_VERSION_ID)
                .requestId(command.getRequestId())
                .traceId(command.getTraceId())
                .promptMessagesJson(qaContext.promptMessagesJson())
                .inputPayloadJson(qaContext.inputPayloadJson())
                .outputSchemaJson(qaContext.outputSchemaJson())
                .stream(false)
                .forceJson(true)
                .locale(DEFAULT_LOCALE)
                .build();
    }

    private QaSessionResult toSessionResult(QaSession session) {
        return new QaSessionResult(
                session.getSessionId(),
                session.getOwnerUserId(),
                session.getTitle(),
                session.getScope(),
                session.getContextMode(),
                session.getContextContentType(),
                session.getContextContentId(),
                session.getStatus(),
                session.getOpenedAt() == null ? null : session.getOpenedAt().getTime(),
                session.getLastMessageAt() == null
                        ? null
                        : session.getLastMessageAt().getTime());
    }

    private void validateOpenSessionCommand(OpenQaSessionCommand command) {
        if (command == null || command.getOwnerUserId() == null) {
            throw new BizException(
                    "DISCOVERY-30002", "discovery.qa.open-session.invalid", "Open QA session command is invalid");
        }
    }

    private void validateAskQuestionCommand(AskQuestionCommand command) {
        if (command == null || command.getSessionId() == null || StringUtils.isBlank(command.getQuestion())) {
            throw new BizException(
                    "DISCOVERY-30003", "discovery.qa.ask-question.invalid", "Ask question command is invalid");
        }
    }

    private QaMessageResult toMessageResult(QaMessage message) {
        if (message == null) {
            return null;
        }
        return new QaMessageResult(
                message.getMessageId(),
                message.getSessionId(),
                message.getRole(),
                message.getContent(),
                message.getMessageStatus(),
                message.getContextTurnCount(),
                message.getFailureReason(),
                message.getSentAt(),
                message.getAnsweredAt());
    }
}
