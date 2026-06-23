package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRefinementLineageRelationCommand {
    private Long refinementTaskId;
    private Long relationId;
    private String relationKey;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String sourceRefsJson;
    private Integer sortOrder;
    private Long operatorId;
}
