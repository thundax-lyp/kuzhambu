package com.thundax.kuzhambu.knowledge.application.graph.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeLineageNodeResult {
    private Long nodeId;
    private String nodeKey;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Long firstExtractedAt;
    private Long lastExtractedAt;
    private Long confirmedAt;
}
