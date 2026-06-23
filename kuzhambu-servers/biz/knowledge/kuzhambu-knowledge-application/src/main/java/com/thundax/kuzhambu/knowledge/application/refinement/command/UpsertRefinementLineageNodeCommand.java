package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRefinementLineageNodeCommand {
    private Long refinementTaskId;
    private Long nodeId;
    private String nodeKey;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String sourceRefsJson;
    private Integer sortOrder;
    private Long operatorId;
}
