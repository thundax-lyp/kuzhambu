package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementRelationDraft {
    private Long id;
    private Long draftId;
    private Long refinementTaskId;
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
    private Long createdBy;
    private Date createdAt;
    private Long updatedBy;
    private Date updatedAt;
}
