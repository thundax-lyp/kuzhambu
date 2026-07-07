package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReextractLowQualityCategoryResult {
    private Long reportId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private String sourceContentType;
    private Long sourceContentId;
    private Long taskId;
    private Long batchJobId;
    private String taskType;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
}
