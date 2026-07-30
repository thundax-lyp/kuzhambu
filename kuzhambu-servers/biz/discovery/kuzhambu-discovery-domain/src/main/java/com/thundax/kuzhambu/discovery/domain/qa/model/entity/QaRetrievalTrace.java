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
public class QaRetrievalTrace {
    private Long id;
    private Long messageId;
    private String rawQuestion;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemIds;
    private String externalChatId;
    private String providerRequestId;
    private Long latencyMs;
    private String failureReason;
    private String raw;
    private Long aiCallId;
    private String aiStatus;
    private String aiErrorType;
    private String aiErrorMessage;
    private Date retrievedAt;

    public QaRetrievalTrace(
            Long id,
            Long traceId,
            Long messageId,
            String rawQuestion,
            String provider,
            String externalKnowledgeBaseId,
            String externalKnowledgeItemIds,
            String externalChatId,
            String providerRequestId,
            Long latencyMs,
            String failureReason,
            String raw,
            Long aiCallId,
            String aiStatus,
            String aiErrorType,
            String aiErrorMessage,
            Date retrievedAt) {
        this.id = id == null ? traceId : id;
        this.messageId = messageId;
        this.rawQuestion = rawQuestion;
        this.provider = provider;
        this.externalKnowledgeBaseId = externalKnowledgeBaseId;
        this.externalKnowledgeItemIds = externalKnowledgeItemIds;
        this.externalChatId = externalChatId;
        this.providerRequestId = providerRequestId;
        this.latencyMs = latencyMs;
        this.failureReason = failureReason;
        this.raw = raw;
        this.aiCallId = aiCallId;
        this.aiStatus = aiStatus;
        this.aiErrorType = aiErrorType;
        this.aiErrorMessage = aiErrorMessage;
        this.retrievedAt = retrievedAt;
    }

    public Long getTraceId() {
        return id;
    }

    public void setTraceId(Long traceId) {
        this.id = traceId;
    }
}
