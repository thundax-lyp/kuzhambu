package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEntityResult {
    private Long entityId;
    private String entityKey;
    private String name;
    private String entityType;
    private String description;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Long firstExtractedAt;
    private Long lastExtractedAt;
    private Long confirmedAt;
}
