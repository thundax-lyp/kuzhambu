package com.thundax.kuzhambu.discovery.application.qa.result;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaTraceResult {
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
    private Instant retrievedAt;

    public Long getTraceId() {
        return id;
    }

    public void setTraceId(Long traceId) {
        this.id = traceId;
    }
}
