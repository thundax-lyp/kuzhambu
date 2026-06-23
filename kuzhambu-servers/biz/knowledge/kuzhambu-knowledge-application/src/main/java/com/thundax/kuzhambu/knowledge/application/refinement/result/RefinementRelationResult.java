package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementRelationResult {
    private Long draftId;
    private Long relationId;
    private String relationKey;
    private String originType;
    private String operationType;
    private String sourceEntityKey;
    private String targetEntityKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private String sourceRefsJson;
    private Integer sortOrder;
}
