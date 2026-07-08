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
    private Long traceId;
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
}
