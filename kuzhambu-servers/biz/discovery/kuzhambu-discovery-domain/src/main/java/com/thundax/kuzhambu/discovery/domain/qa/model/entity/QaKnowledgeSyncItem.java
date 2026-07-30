package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeContentRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.KnowledgeSourceId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaKnowledgeSyncStatus;
import java.time.Instant;
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
    private KnowledgeSourceId sourceId;
    private KnowledgeContentRef content;
    private String knowledgeBaseName;
    private Integer currentVersionNo;
    private String knowledgeRevision;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemId;
    private QaKnowledgeSyncStatus syncStatus;
    private String failureReason;
    private Instant syncedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public QaKnowledgeSyncItem(
            Long id,
            String sourceId,
            String contentType,
            Long contentId,
            String knowledgeBaseName,
            Integer currentVersionNo,
            String knowledgeRevision,
            String provider,
            String externalKnowledgeBaseId,
            String externalKnowledgeItemId,
            String syncStatus,
            String failureReason,
            Instant syncedAt,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.sourceId = QaStringValueCodec.toKnowledgeSourceId(sourceId);
        this.content = QaStringValueCodec.toKnowledgeContentRef(contentType, contentId);
        this.knowledgeBaseName = knowledgeBaseName;
        this.currentVersionNo = currentVersionNo;
        this.knowledgeRevision = knowledgeRevision;
        this.provider = provider;
        this.externalKnowledgeBaseId = externalKnowledgeBaseId;
        this.externalKnowledgeItemId = externalKnowledgeItemId;
        this.syncStatus = QaStringValueCodec.toKnowledgeSyncStatus(syncStatus);
        this.failureReason = failureReason;
        this.syncedAt = syncedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getContentType() {
        return content == null ? null : content.getContentType();
    }

    public void setContentType(String contentType) {
        this.content = QaStringValueCodec.toKnowledgeContentRef(contentType, getContentId());
    }

    public Long getContentId() {
        return content == null ? null : content.getContentId();
    }

    public void setContentId(Long contentId) {
        this.content = QaStringValueCodec.toKnowledgeContentRef(getContentType(), contentId);
    }

    public void setSourceId(String sourceId) {
        this.sourceId = QaStringValueCodec.toKnowledgeSourceId(sourceId);
    }

    public void setSourceId(KnowledgeSourceId sourceId) {
        this.sourceId = sourceId;
    }

    public void setSyncStatus(QaKnowledgeSyncStatus syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = QaStringValueCodec.toKnowledgeSyncStatus(syncStatus);
    }
}
