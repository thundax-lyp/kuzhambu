package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelationResult {
    private Long relationId;
    private String relationKey;
    private String sourceName;
    private String sourceType;
    private String targetName;
    private String targetType;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Long firstExtractedAt;
    private Long lastExtractedAt;
    private Long confirmedAt;
}
