package com.thundax.kuzhambu.discovery.domain.qa.model.entity;

import com.thundax.kuzhambu.discovery.domain.qa.codec.QaSessionIdCodec;
import com.thundax.kuzhambu.discovery.domain.qa.codec.QaStringValueCodec;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaContextContentRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaOwnerRef;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionId;
import com.thundax.kuzhambu.discovery.domain.qa.model.valueobject.QaSessionStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSession {
    private static final String REMOVED_STATUS = "REMOVED";

    private QaSessionId id;
    private QaOwnerRef owner;
    private String knowledgeBaseName;
    private String title;
    private String scope;
    private String contextMode;
    private QaContextContentRef contextContent;
    private QaSessionStatus status;
    private Date openedAt;
    private Date lastMessageAt;
    private Date removedAt;

    public QaSession(
            Long id,
            Long sessionId,
            String ownerType,
            String ownerId,
            String knowledgeBaseName,
            String title,
            String scope,
            String contextMode,
            String contextContentType,
            Long contextContentId,
            String status,
            Date openedAt,
            Date lastMessageAt,
            Date removedAt) {
        this.id = QaSessionIdCodec.toDomain(id == null ? sessionId : id);
        this.owner = QaStringValueCodec.toOwnerRef(ownerType, ownerId);
        this.knowledgeBaseName = knowledgeBaseName;
        this.title = title;
        this.scope = scope;
        this.contextMode = contextMode;
        this.contextContent = QaStringValueCodec.toContextContentRef(contextContentType, contextContentId);
        this.status = QaStringValueCodec.toSessionStatus(status);
        this.openedAt = openedAt;
        this.lastMessageAt = lastMessageAt;
        this.removedAt = removedAt;
    }

    public void markRemoved(Date removedAt) {
        this.status = new QaSessionStatus(REMOVED_STATUS);
        this.removedAt = removedAt;
    }

    public boolean isRemoved() {
        return removedAt != null || (status != null && REMOVED_STATUS.equals(status.value()));
    }

    public QaSessionId getSessionId() {
        return id;
    }

    public void setSessionId(QaSessionId sessionId) {
        this.id = sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.id = QaSessionIdCodec.toDomain(sessionId);
    }

    public String getOwnerType() {
        return owner == null ? null : owner.getOwnerType();
    }

    public void setOwnerType(String ownerType) {
        this.owner = QaStringValueCodec.toOwnerRef(ownerType, getOwnerId());
    }

    public String getOwnerId() {
        return owner == null ? null : owner.getOwnerId();
    }

    public void setOwnerId(String ownerId) {
        this.owner = QaStringValueCodec.toOwnerRef(getOwnerType(), ownerId);
    }

    public String getContextContentType() {
        return contextContent == null ? null : contextContent.getContentType();
    }

    public void setContextContentType(String contextContentType) {
        this.contextContent = QaStringValueCodec.toContextContentRef(contextContentType, getContextContentId());
    }

    public Long getContextContentId() {
        return contextContent == null ? null : contextContent.getContentId();
    }

    public void setContextContentId(Long contextContentId) {
        this.contextContent = QaStringValueCodec.toContextContentRef(getContextContentType(), contextContentId);
    }

    public void setStatus(QaSessionStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = QaStringValueCodec.toSessionStatus(status);
    }
}
