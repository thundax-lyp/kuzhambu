package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeLineageRelation {
    private Long id;
    private String relationKey;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Instant firstExtractedAt;
    private Instant lastExtractedAt;
    private Instant confirmedAt;
}
