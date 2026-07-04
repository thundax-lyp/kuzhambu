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
    private Long id;
    private Long sessionId;
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
}
