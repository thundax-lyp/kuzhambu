package com.thundax.kuzhambu.discovery.application.qa.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSyncItemResult {
    private String sourceId;
    private String contentType;
    private Long contentId;
    private String title;
    private String knowledgeBaseName;
    private Integer currentVersionNo;
    private String knowledgeRevision;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemId;
    private String syncStatus;
    private String failureReason;
    private Long syncedAt;
    private Long createdAt;
    private Long updatedAt;
}
