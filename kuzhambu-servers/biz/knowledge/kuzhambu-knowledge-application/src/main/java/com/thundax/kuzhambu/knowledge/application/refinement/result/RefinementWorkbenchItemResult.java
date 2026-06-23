package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementWorkbenchItemResult {
    private Long refinementTaskId;
    private Long graphVersionId;
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private String status;
    private Long openedBy;
    private Long openedAt;
    private RefinementProgressSummaryResult progressSummary;
}
