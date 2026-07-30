package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementLineageNodeDraft {
    private Long id;
    private Long draftId;
    private Long refinementTaskId;
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
    private Long createdBy;
    private Instant createdAt;
    private Long updatedBy;
    private Instant updatedAt;
}
