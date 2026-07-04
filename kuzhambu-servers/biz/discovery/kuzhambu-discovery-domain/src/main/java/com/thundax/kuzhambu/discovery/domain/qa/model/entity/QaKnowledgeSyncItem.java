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
public class QaKnowledgeSyncItem {
    private Long id;
    private String sourceId;
    private String contentType;
    private Long contentId;
    private String knowledgeBaseName;
    private Integer currentVersionNo;
    private String knowledgeRevision;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemId;
    private String syncStatus;
    private String failureReason;
    private Date syncedAt;
    private Date createdAt;
    private Date updatedAt;
}
