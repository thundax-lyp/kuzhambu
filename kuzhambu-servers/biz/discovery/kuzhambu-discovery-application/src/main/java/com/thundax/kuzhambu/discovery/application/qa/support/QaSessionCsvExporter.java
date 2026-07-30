package com.thundax.kuzhambu.discovery.application.qa.support;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaMessage;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaRetrievalTrace;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSession;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QaSessionCsvExporter {

    private static final String[] HEADER = {
        "rowType",
        "sessionId",
        "title",
        "ownerType",
        "ownerId",
        "scope",
        "contextMode",
        "contextContentType",
        "contextContentId",
        "status",
        "openedAt",
        "lastMessageAt",
        "removedAt",
        "messageId",
        "role",
        "content",
        "answerStatus",
        "model",
        "sentAt",
        "answeredAt",
        "sourceId",
        "sourceBusinessId",
        "sourceTitle",
        "sourceStatus",
        "sourceRank",
        "traceId",
        "provider",
        "providerRequestId",
        "finishReason",
        "failureReason"
    };

    public String export(
            QaSession session,
            List<QaMessage> messages,
            Map<Long, List<QaSource>> sourcesByMessageId,
            Map<Long, QaRetrievalTrace> tracesByMessageId) {
        StringBuilder builder = new StringBuilder();
        appendRow(builder, (Object[]) HEADER);
        appendSessionRow(builder, session);
        for (QaMessage message : safeMessages(messages)) {
            appendMessageRow(builder, session, message);
            Long messageId = QaMessageIdCodec.toValue(message.getMessageId());
            for (QaSource source : safeSources(sourcesByMessageId, messageId)) {
                appendSourceRow(builder, session, message, source);
            }
            QaRetrievalTrace trace = tracesByMessageId == null ? null : tracesByMessageId.get(messageId);
            if (trace != null) {
                appendTraceRow(builder, session, message, trace);
            }
        }
        return builder.toString();
    }

    private void appendSessionRow(StringBuilder builder, QaSession session) {
        appendRow(
                builder,
                "SESSION",
                QaSessionIdCodec.toValue(session.getSessionId()),
                session.getTitle(),
                session.getOwnerType(),
                session.getOwnerId(),
                session.getScope(),
                session.getContextMode(),
                session.getContextContentType(),
                session.getContextContentId(),
                QaStringValueCodec.toValue(session.getStatus()),
                millis(session.getOpenedAt()),
                millis(session.getLastMessageAt()),
                millis(session.getRemovedAt()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private void appendMessageRow(StringBuilder builder, QaSession session, QaMessage message) {
        appendRow(
                builder,
                "MESSAGE",
                QaSessionIdCodec.toValue(session.getSessionId()),
                session.getTitle(),
                session.getOwnerType(),
                session.getOwnerId(),
                session.getScope(),
                session.getContextMode(),
                session.getContextContentType(),
                session.getContextContentId(),
                QaStringValueCodec.toValue(session.getStatus()),
                millis(session.getOpenedAt()),
                millis(session.getLastMessageAt()),
                millis(session.getRemovedAt()),
                QaMessageIdCodec.toValue(message.getMessageId()),
                QaStringValueCodec.toValue(message.getRole()),
                message.getContent(),
                message.getAnswerStatus(),
                message.getModel(),
                millis(message.getSentAt()),
                millis(message.getAnsweredAt()),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                message.getFinishReason(),
                message.getFailureReason());
    }

    private void appendSourceRow(StringBuilder builder, QaSession session, QaMessage message, QaSource source) {
        appendRow(
                builder,
                "SOURCE",
                QaSessionIdCodec.toValue(session.getSessionId()),
                session.getTitle(),
                session.getOwnerType(),
                session.getOwnerId(),
                session.getScope(),
                session.getContextMode(),
                session.getContextContentType(),
                session.getContextContentId(),
                QaStringValueCodec.toValue(session.getStatus()),
                millis(session.getOpenedAt()),
                millis(session.getLastMessageAt()),
                millis(session.getRemovedAt()),
                QaMessageIdCodec.toValue(message.getMessageId()),
                QaStringValueCodec.toValue(message.getRole()),
                null,
                message.getAnswerStatus(),
                message.getModel(),
                millis(message.getSentAt()),
                millis(message.getAnsweredAt()),
                source.getSourceId(),
                source.getSourceBusinessId(),
                source.getTitleSnapshot(),
                source.getSourceStatus(),
                source.getSourceRank(),
                null,
                null,
                null,
                null,
                null);
    }

    private void appendTraceRow(StringBuilder builder, QaSession session, QaMessage message, QaRetrievalTrace trace) {
        appendRow(
                builder,
                "TRACE",
                QaSessionIdCodec.toValue(session.getSessionId()),
                session.getTitle(),
                session.getOwnerType(),
                session.getOwnerId(),
                session.getScope(),
                session.getContextMode(),
                session.getContextContentType(),
                session.getContextContentId(),
                QaStringValueCodec.toValue(session.getStatus()),
                millis(session.getOpenedAt()),
                millis(session.getLastMessageAt()),
                millis(session.getRemovedAt()),
                QaMessageIdCodec.toValue(message.getMessageId()),
                QaStringValueCodec.toValue(message.getRole()),
                null,
                message.getAnswerStatus(),
                message.getModel(),
                millis(message.getSentAt()),
                millis(message.getAnsweredAt()),
                null,
                null,
                null,
                null,
                null,
                trace.getTraceId(),
                trace.getProvider(),
                trace.getProviderRequestId(),
                message.getFinishReason(),
                trace.getFailureReason());
    }

    private void appendRow(StringBuilder builder, Object... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(escape(values[index]));
        }
        builder.append('\n');
    }

    private String escape(Object value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + String.valueOf(value).replace("\"", "\"\"") + "\"";
    }

    private Long millis(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    private List<QaMessage> safeMessages(List<QaMessage> messages) {
        return messages == null ? List.of() : messages;
    }

    private List<QaSource> safeSources(Map<Long, List<QaSource>> sourcesByMessageId, Long messageId) {
        if (sourcesByMessageId == null || messageId == null) {
            return List.of();
        }
        List<QaSource> sources = sourcesByMessageId.get(messageId);
        return sources == null ? List.of() : sources;
    }
}
