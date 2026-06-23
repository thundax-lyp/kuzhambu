package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementLineageNodeResult {
    private Long draftId;
    private Long nodeId;
    private String nodeKey;
    private String originType;
    private String operationType;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String confirmationStatus;
    private String sourceRefsJson;
    private Integer sortOrder;
}
