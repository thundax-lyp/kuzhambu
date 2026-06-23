package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementEntityResult {
    private Long draftId;
    private Long entityId;
    private String entityKey;
    private String originType;
    private String operationType;
    private String name;
    private String entityType;
    private String description;
    private String confirmationStatus;
    private String sourceRefsJson;
    private Integer sortOrder;
}
