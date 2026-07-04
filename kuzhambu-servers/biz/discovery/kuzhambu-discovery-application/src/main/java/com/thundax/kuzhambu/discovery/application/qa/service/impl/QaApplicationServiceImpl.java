package com.thundax.kuzhambu.discovery.application.qa.service.impl;

import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import com.thundax.kuzhambu.discovery.application.qa.command.DeleteQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.ExportQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.command.OpenQaSessionCommand;
import com.thundax.kuzhambu.discovery.application.qa.result.QaMessageResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionDetailResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionExportResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSessionResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.application.qa.result.QaTraceResult;
import com.thundax.kuzhambu.discovery.application.qa.service.QaApplicationService;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSessionCsvExporter;
import com.thundax.kuzhambu.discovery.application.qa.support.QaSourceAssembler;
import com.thundax.kuzhambu.discovery.application.qa.support.QaTraceAssembler;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSessionExport;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaMessageRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaRetrievalTraceRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionExportRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSessionRepository;
import com.thundax.kuzhambu.discovery.domain.qa.repository.QaSourceRepository;
import com.thundax.kuzhambu.storage.facade.StorageFacade;
import com.thundax.kuzhambu.storage.facade.request.UploadStorageFacadeRequest;
import com.thundax.kuzhambu.storage.facade.response.UploadStorageFacadeResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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
public class QaApplicationServiceImpl implements QaApplicationService {

    private static final String DEFAULT_OWNER_TYPE = "USER";
    private static final String DEFAULT_KNOWLEDGE_BASE_NAME = "kuzhambu-qa";
    private static final String SESSION_ALREADY_REMOVED_CODE = "QA_SESSION_ALREADY_REMOVED";
    private static final String SESSION_ALREADY_REMOVED_MESSAGE = "QA session has already been removed";
    private static final String EXPORT_FORMAT_CSV = "CSV";
    private static final String EXPORT_STATUS_PROCESSING = "PROCESSING";
    private static final String EXPORT_STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String EXPORT_STATUS_FAILED = "FAILED";
    private static final String EXPORT_CONTENT_TYPE = "text/csv; charset=UTF-8";
    private static final String EXPORT_OWNER_TYPE = "DISCOVERY_QA_SESSION_EXPORT";
    private final QaSessionRepository qaSessionRepository;
    private final QaMessageRepository qaMessageRepository;
    private final QaSourceRepository qaSourceRepository;
    private final QaRetrievalTraceRepository qaRetrievalTraceRepository;
    private final QaSessionExportRepository qaSessionExportRepository;
    private final StorageFacade storageFacade;
    private final QaSessionCsvExporter qaSessionCsvExporter;
    private final QaSourceAssembler qaSourceAssembler;
    private final QaTraceAssembler qaTraceAssembler;

    public QaApplicationServiceImpl(
            QaSessionRepository qaSessionRepository,
            QaMessageRepository qaMessageRepository,
            QaSourceRepository qaSourceRepository,
            QaRetrievalTraceRepository qaRetrievalTraceRepository,
            QaSessionExportRepository qaSessionExportRepository,
            StorageFacade storageFacade,
            QaSessionCsvExporter qaSessionCsvExporter,
            QaSourceAssembler qaSourceAssembler,
            QaTraceAssembler qaTraceAssembler) {
        this.qaSessionRepository = qaSessionRepository;
        this.qaMessageRepository = qaMessageRepository;
        this.qaSourceRepository = qaSourceRepository;
        this.qaRetrievalTraceRepository = qaRetrievalTraceRepository;
        this.qaSessionExportRepository = qaSessionExportRepository;
        this.storageFacade = storageFacade;
        this.qaSessionCsvExporter = qaSessionCsvExporter;
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
    public void deleteSession(DeleteQaSessionCommand command) {
        if (command == null || command.getSessionId() == null) {
            throw new BizException("DISCOVERY-30006", "discovery.qa.session-id.required", "Session id is required");
        }
        QaSession session = requireSession(command.getSessionId());
        if (session.isRemoved()) {
            throw removedSessionException();
        }
        if (!Boolean.TRUE.equals(command.getAdminOperation())) {
            requireOwner(session, command.getOwnerType(), command.getOwnerId());
        }
        int updated = qaSessionRepository.markRemoved(command.getSessionId(), new Date());
        if (updated == 0) {
            QaSession latest = qaSessionRepository.getBySessionId(command.getSessionId());
            if (latest == null) {
                throw sessionNotFoundException();
            }
            if (latest.isRemoved()) {
                throw removedSessionException();
            }
        }
    }

    @Override
    public QaSessionExportResult exportSession(ExportQaSessionCommand command) {
        validateExportCommand(command);
        QaSession session = requireSession(command.getSessionId());
        if (!Boolean.TRUE.equals(command.getAdminOperation())) {
            requireOwner(session, command.getOwnerType(), command.getOwnerId());
            if (session.isRemoved()) {
                throw removedSessionException();
            }
        }
        Date requestedAt = new Date();
        QaSessionExport export = new QaSessionExport(
                null,
                null,
                session.getSessionId(),
                EXPORT_FORMAT_CSV,
                null,
                EXPORT_STATUS_PROCESSING,
                null,
                command.getRequesterUserId(),
                requestedAt,
                null);
        Long exportId = qaSessionExportRepository.save(export);
        export.setId(exportId);
        export.setExportId(exportId);
        String filename = exportFilename(session.getSessionId(), exportId);
        try {
            byte[] content = buildCsvContent(session);
            UploadStorageFacadeResponse uploadResponse = storageFacade.upload(UploadStorageFacadeRequest.builder()
                    .inputStream(new ByteArrayInputStream(content))
                    .originalFilename(filename)
                    .contentType(EXPORT_CONTENT_TYPE)
                    .sizeBytes((long) content.length)
                    .allowedSuffixes(List.of("csv"))
                    .ownerType(EXPORT_OWNER_TYPE)
                    .ownerId(exportOwnerId(session.getSessionId(), exportId))
                    .build());
            if (uploadResponse == null || uploadResponse.getStorageObjectId() == null) {
                throw new IllegalStateException("Storage upload response is empty");
            }
            export.setStorageObjectId(uploadResponse.getStorageObjectId());
            export.setExportStatus(EXPORT_STATUS_SUCCEEDED);
            export.setCompletedAt(new Date());
            qaSessionExportRepository.update(export);
            return toExportResult(export, filename);
        } catch (RuntimeException ex) {
            export.setExportStatus(EXPORT_STATUS_FAILED);
            export.setFailureReason(
                    StringUtils.defaultIfBlank(ex.getMessage(), ex.getClass().getSimpleName()));
            export.setCompletedAt(new Date());
            qaSessionExportRepository.update(export);
            return toExportResult(export, filename);
        }
    }

    @Override
    public List<QaSessionResult> listPortalSessions(String ownerType, String ownerId, Integer limit) {
        return qaSessionRepository.listByOwnerUserId(ownerType, ownerId, limit).stream()
                .map(this::toSessionResult)
                .toList();
    }

    @Override
    public QaSessionDetailResult getPortalSessionDetail(Long sessionId, String ownerType, String ownerId) {
        QaSession session = requireSession(sessionId);
        requireOwner(session, ownerType, ownerId);
        if (session.isRemoved()) {
            throw removedSessionException();
        }
        return toSessionDetailResult(session);
    }

    @Override
    public QaSessionDetailResult getSessionDetail(Long sessionId) {
        if (sessionId == null) {
            throw new BizException("DISCOVERY-30006", "discovery.qa.session-id.required", "Session id is required");
        }
        QaSession session = requireSession(sessionId);
        return toSessionDetailResult(session);
    }

    private QaSessionDetailResult toSessionDetailResult(QaSession session) {
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
        result.setRemovedAt(
                session.getRemovedAt() == null ? null : session.getRemovedAt().getTime());
        List<QaMessage> messages = qaMessageRepository.listBySessionId(session.getSessionId());
        result.setMessages((messages == null ? List.<QaMessage>of() : messages)
                .stream().map(this::toMessageResult).toList());
        return result;
    }

    private byte[] buildCsvContent(QaSession session) {
        List<QaMessage> messages = qaMessageRepository.listBySessionId(session.getSessionId());
        List<QaMessage> safeMessages = messages == null ? List.of() : messages;
        Map<Long, List<QaSource>> sourcesByMessageId = safeMessages.stream()
                .filter(message -> message.getMessageId() != null)
                .collect(Collectors.toMap(QaMessage::getMessageId, message -> {
                    List<QaSource> sources = qaSourceRepository.listByMessageId(message.getMessageId());
                    return sources == null ? List.of() : sources;
                }));
        Map<Long, QaRetrievalTrace> tracesByMessageId = safeMessages.stream()
                .filter(message -> message.getMessageId() != null)
                .map(message -> qaRetrievalTraceRepository.getByMessageId(message.getMessageId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(QaRetrievalTrace::getMessageId, trace -> trace));
        return qaSessionCsvExporter
                .export(session, safeMessages, sourcesByMessageId, tracesByMessageId)
                .getBytes(StandardCharsets.UTF_8);
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
                        : session.getLastMessageAt().getTime(),
                session.getRemovedAt() == null ? null : session.getRemovedAt().getTime());
    }

    private void validateOpenSessionCommand(OpenQaSessionCommand command) {
        if (command == null || command.getOwnerUserId() == null) {
            throw new BizException(
                    "DISCOVERY-30002", "discovery.qa.open-session.invalid", "Open QA session command is invalid");
        }
    }

    private void validateExportCommand(ExportQaSessionCommand command) {
        if (command == null || command.getSessionId() == null) {
            throw new BizException("DISCOVERY-30006", "discovery.qa.session-id.required", "Session id is required");
        }
        if (StringUtils.isNotBlank(command.getFormat()) && !EXPORT_FORMAT_CSV.equalsIgnoreCase(command.getFormat())) {
            throw new BizException(
                    "DISCOVERY-30010", "discovery.qa.export-format.invalid", "Only CSV export is supported");
        }
    }

    private QaSession requireSession(Long sessionId) {
        if (sessionId == null) {
            throw new BizException("DISCOVERY-30006", "discovery.qa.session-id.required", "Session id is required");
        }
        QaSession session = qaSessionRepository.getBySessionId(sessionId);
        if (session == null) {
            throw sessionNotFoundException();
        }
        return session;
    }

    private void requireOwner(QaSession session, String ownerType, String ownerId) {
        if (session == null
                || !StringUtils.equals(session.getOwnerType(), ownerType)
                || !StringUtils.equals(session.getOwnerId(), ownerId)) {
            throw new BizException(
                    "DISCOVERY-30009", "discovery.qa.session.forbidden", "QA session owner does not match");
        }
    }

    private BizException sessionNotFoundException() {
        return new BizException("DISCOVERY-30001", "discovery.qa.session.not-found", "QA session does not exist");
    }

    private BizException removedSessionException() {
        return new BizException(
                SESSION_ALREADY_REMOVED_CODE, "discovery.qa.session.already-removed", SESSION_ALREADY_REMOVED_MESSAGE);
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

    private QaSessionExportResult toExportResult(QaSessionExport export, String filename) {
        return new QaSessionExportResult(
                export.getExportId(),
                export.getSessionId(),
                export.getFormat(),
                export.getStorageObjectId(),
                export.getExportStatus(),
                export.getFailureReason(),
                export.getRequestedAt() == null ? null : export.getRequestedAt().getTime(),
                export.getCompletedAt() == null ? null : export.getCompletedAt().getTime(),
                filename,
                EXPORT_CONTENT_TYPE);
    }

    private String exportFilename(Long sessionId, Long exportId) {
        return "discovery-qa-session-" + sessionId + "-" + exportId + ".csv";
    }

    private String exportOwnerId(Long sessionId, Long exportId) {
        return "session:" + sessionId + ":export:" + exportId;
    }
}
