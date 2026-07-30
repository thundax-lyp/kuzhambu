package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaMessageIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaMessageRole;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaMessage {
    private QaMessageId id;
    private QaSessionId sessionId;
    private QaMessageRole role;
    private String content;
    private String answerStatus;
    private String model;
    private Integer contextTurnCount;
    private String failureReason;
    private String providerChatId;
    private String finishReason;
    private Date sentAt;
    private Date answeredAt;

    public QaMessage(
            Long id,
            Long messageId,
            Long sessionId,
            String role,
            String content,
            String answerStatus,
            String model,
            Integer contextTurnCount,
            String failureReason,
            String providerChatId,
            String finishReason,
            Date sentAt,
            Date answeredAt) {
        this.id = QaMessageIdCodec.toDomain(id == null ? messageId : id);
        this.sessionId = QaSessionIdCodec.toDomain(sessionId);
        this.role = QaStringValueCodec.toMessageRole(role);
        this.content = content;
        this.answerStatus = answerStatus;
        this.model = model;
        this.contextTurnCount = contextTurnCount;
        this.failureReason = failureReason;
        this.providerChatId = providerChatId;
        this.finishReason = finishReason;
        this.sentAt = sentAt;
        this.answeredAt = answeredAt;
    }

    public QaMessageId getMessageId() {
        return id;
    }

    public void setMessageId(QaMessageId messageId) {
        this.id = messageId;
    }

    public void setMessageId(Long messageId) {
        this.id = QaMessageIdCodec.toDomain(messageId);
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = QaSessionIdCodec.toDomain(sessionId);
    }

    public void setRole(String role) {
        this.role = QaStringValueCodec.toMessageRole(role);
    }
}
