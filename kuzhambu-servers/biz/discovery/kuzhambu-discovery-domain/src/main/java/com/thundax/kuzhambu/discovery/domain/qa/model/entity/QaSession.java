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
public class QaSession {
    private static final String REMOVED_STATUS = "REMOVED";

    private Long id;
    private String ownerType;
    private String ownerId;
    private String knowledgeBaseName;
    private String title;
    private String scope;
    private String contextMode;
    private String contextContentType;
    private Long contextContentId;
    private String status;
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
        this.id = id == null ? sessionId : id;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.knowledgeBaseName = knowledgeBaseName;
        this.title = title;
        this.scope = scope;
        this.contextMode = contextMode;
        this.contextContentType = contextContentType;
        this.contextContentId = contextContentId;
        this.status = status;
        this.openedAt = openedAt;
        this.lastMessageAt = lastMessageAt;
        this.removedAt = removedAt;
    }

    public void markRemoved(Date removedAt) {
        this.status = REMOVED_STATUS;
        this.removedAt = removedAt;
    }

    public boolean isRemoved() {
        return removedAt != null || REMOVED_STATUS.equals(status);
    }

    public Long getSessionId() {
        return id;
    }

    public void setSessionId(Long sessionId) {
        this.id = sessionId;
    }
}
