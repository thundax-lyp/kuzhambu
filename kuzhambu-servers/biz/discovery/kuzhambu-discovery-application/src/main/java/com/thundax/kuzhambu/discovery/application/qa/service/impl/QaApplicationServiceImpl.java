package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@BizExceptionBoundary
public class QaApplicationServiceImpl implements QaApplicationService {

    private static final String DEFAULT_OWNER_TYPE = "USER";
    private static final String DEFAULT_KNOWLEDGE_BASE_NAME = "kuzhambu-qa";
    private final QaSessionRepository qaSessionRepository;
    private final QaMessageRepository qaMessageRepository;
    private final QaSourceRepository qaSourceRepository;
    private final QaRetrievalTraceRepository qaRetrievalTraceRepository;
    private final QaSourceAssembler qaSourceAssembler;
    private final QaTraceAssembler qaTraceAssembler;

    public QaApplicationServiceImpl(
            QaSessionRepository qaSessionRepository,
            QaMessageRepository qaMessageRepository,
            QaSourceRepository qaSourceRepository,
            QaRetrievalTraceRepository qaRetrievalTraceRepository,
            QaSourceAssembler qaSourceAssembler,
            QaTraceAssembler qaTraceAssembler) {
        this.qaSessionRepository = qaSessionRepository;
        this.qaMessageRepository = qaMessageRepository;
        this.qaSourceRepository = qaSourceRepository;
        this.qaRetrievalTraceRepository = qaRetrievalTraceRepository;
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
                DEFAULT_OWNER_TYPE,
                String.valueOf(command.getOwnerUserId()),
                DEFAULT_KNOWLEDGE_BASE_NAME,
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
        result.setOwnerUserId(parseOwnerUserId(session.getOwnerId()));
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

    private QaSessionResult toSessionResult(QaSession session) {
        return new QaSessionResult(
                session.getSessionId(),
                parseOwnerUserId(session.getOwnerId()),
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

    private Long parseOwnerUserId(String ownerId) {
        if (StringUtils.isBlank(ownerId)) {
            return null;
        }
        try {
            return Long.valueOf(ownerId);
        } catch (NumberFormatException ex) {
            return null;
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
                message.getAnswerStatus(),
                message.getContextTurnCount(),
                message.getFailureReason(),
                message.getSentAt(),
                message.getAnsweredAt());
    }
}
