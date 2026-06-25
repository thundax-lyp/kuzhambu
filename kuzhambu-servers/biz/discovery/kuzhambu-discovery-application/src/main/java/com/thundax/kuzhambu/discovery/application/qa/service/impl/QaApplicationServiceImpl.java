package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import com.thundax.kuzhambu.ai.domain.discovery.service.DiscoveryAiDomainService;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.qa.command.AskQuestionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaAnswerResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
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
    private final ClassicsSearchContentApplicationService classicsSearchContentApplicationService;
    private final DiscoveryAiDomainService discoveryAiDomainService;
    private final QaContextAssembler qaContextAssembler;
    private final QaSourceAssembler qaSourceAssembler;
    private final QaTraceAssembler qaTraceAssembler;

    public QaApplicationServiceImpl(
            QaSessionRepository qaSessionRepository,
            QaMessageRepository qaMessageRepository,
            QaSourceRepository qaSourceRepository,
            QaRetrievalTraceRepository qaRetrievalTraceRepository,
            QueryUnderstandingApplicationService queryUnderstandingApplicationService,
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService,
            DiscoveryAiDomainService discoveryAiDomainService,
            QaContextAssembler qaContextAssembler,
            QaSourceAssembler qaSourceAssembler,
            QaTraceAssembler qaTraceAssembler) {
        this.qaSessionRepository = qaSessionRepository;
        this.qaMessageRepository = qaMessageRepository;
        this.qaSourceRepository = qaSourceRepository;
        this.qaRetrievalTraceRepository = qaRetrievalTraceRepository;
        this.queryUnderstandingApplicationService = queryUnderstandingApplicationService;
        this.classicsSearchContentApplicationService = classicsSearchContentApplicationService;
        this.discoveryAiDomainService = discoveryAiDomainService;
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
        List<ClassicsSearchSourceContent> publicContents = classicsSearchContentApplicationService.listPublicContents();
        QaContextAssembler.QaContext qaContext =
                qaContextAssembler.assemble(command.getQuestion(), understandingResult, publicContents);
        DiscoveryAiResult aiResult = discoveryAiDomainService.generateAnswer(toAiRequest(command, qaContext));

        String answerText = aiResult == null ? null : aiResult.getResultPayload();
        if (StringUtils.isBlank(answerText)) {
            answerText = "暂时没有生成可用回答。";
        }

        QaMessage assistantMessage = new QaMessage(
                null,
                null,
                command.getSessionId(),
                "ASSISTANT",
                answerText,
                "ANSWERED",
                command.getContextTurnCount() == null ? 0 : command.getContextTurnCount(),
                null,
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

        QaRetrievalTrace traceEntity =
                qaTraceAssembler.toDomain(command, session, understandingResult, qaContext, answerMessagePk);
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
                "SUCCEEDED",
                null,
                qaSourceAssembler.toResultList(sourceEntities),
                qaTraceAssembler.toTraceSummary(traceEntity, understandingResult, qaContext));
    }

    private DiscoveryAiRequest toAiRequest(AskQuestionCommand command, QaContextAssembler.QaContext qaContext) {
        return new DiscoveryAiRequest(
                DEFAULT_SERVICE_ID,
                DEFAULT_SERVICE_ROLE,
                DEFAULT_MODEL_ID,
                DEFAULT_MODEL_NAME,
                DEFAULT_PROMPT_VERSION_ID,
                command.getRequestId(),
                command.getTraceId(),
                qaContext.promptMessagesJson(),
                null,
                null,
                qaContext.inputPayloadJson(),
                qaContext.outputSchemaJson(),
                false,
                true,
                DEFAULT_LOCALE);
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
}
