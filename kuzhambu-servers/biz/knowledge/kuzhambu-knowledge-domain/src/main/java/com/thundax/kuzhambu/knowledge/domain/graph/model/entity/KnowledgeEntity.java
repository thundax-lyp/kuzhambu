package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEntity {
    private Long id;
    private Long entityId;
    private String entityKey;
    private String name;
    private String entityType;
    private String description;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Date firstExtractedAt;
    private Date lastExtractedAt;
    private Date confirmedAt;
}
