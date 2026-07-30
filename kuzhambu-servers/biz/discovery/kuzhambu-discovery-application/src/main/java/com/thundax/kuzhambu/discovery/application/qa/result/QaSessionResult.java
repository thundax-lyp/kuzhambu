package com.thundax.kuzhambu.discovery.application.qa.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QaSessionResult {
    private Long id;
    private Long ownerUserId;
    private String title;
    private String scope;
    private String contextMode;
    private String contextContentType;
    private Long contextContentId;
    private String status;
    private Long openedAt;
    private Long lastMessageAt;
    private Long removedAt;

    public Long getSessionId() {
        return id;
    }

    public void setSessionId(Long sessionId) {
        this.id = sessionId;
    }
}
