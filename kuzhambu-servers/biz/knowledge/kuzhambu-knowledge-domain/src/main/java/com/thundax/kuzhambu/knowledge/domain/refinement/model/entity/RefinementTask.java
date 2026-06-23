package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import com.thundax.kuzhambu.knowledge.domain.refinement.model.valueobject.RefinementTaskId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementTask {
    private Long id;
    private RefinementTaskId refinementTaskId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Long graphVersionId;
    private String status;
    private Long openedBy;
    private Date openedAt;
    private Long submittedBy;
    private Date submittedAt;
    private Long appliedBy;
    private Date appliedAt;
    private Long cancelledBy;
    private Date cancelledAt;
}
