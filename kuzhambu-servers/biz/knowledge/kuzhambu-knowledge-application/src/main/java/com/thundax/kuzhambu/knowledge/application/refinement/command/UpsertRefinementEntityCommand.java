package com.thundax.kuzhambu.knowledge.application.refinement.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRefinementEntityCommand {
    private Long refinementTaskId;
    private Long entityId;
    private String entityKey;
    private String name;
    private String entityType;
    private String description;
    private String sourceRefsJson;
    private Integer sortOrder;
    private Long operatorId;
}
