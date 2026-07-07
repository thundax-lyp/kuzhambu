package com.thundax.kuzhambu.knowledge.application.lineage.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineageCanvasQuery {
    private Long versionId;
    private Long focusNodeId;
    private Long focusRelationId;
    private String keyword;
    private String nodeType;
    private String relationType;
    private String confirmationStatus;
    private Integer depth;
}
