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
public class KnowledgeLineageNode {
    private Long id;
    private String nodeKey;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Date firstExtractedAt;
    private Date lastExtractedAt;
    private Date confirmedAt;
}
