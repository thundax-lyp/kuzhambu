package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

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
    private Long id;
    private Long sessionId;
    private String role;
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
        this.id = id == null ? messageId : id;
        this.sessionId = sessionId;
        this.role = role;
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

    public Long getMessageId() {
        return id;
    }

    public void setMessageId(Long messageId) {
        this.id = messageId;
    }
}
