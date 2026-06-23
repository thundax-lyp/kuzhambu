package com.thundax.kuzhambu.knowledge.application.refinement.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementDetailResult {
    private Long refinementTaskId;
    private Long graphVersionId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private String status;
    private RefinementProgressSummaryResult progressSummary;
    private List<RefinementEntityResult> entities;
    private List<RefinementRelationResult> relations;
    private List<RefinementLineageNodeResult> lineageNodes;
    private List<RefinementLineageRelationResult> lineageRelations;
    private List<RefinementEntityOptionResult> entityOptions;
}
