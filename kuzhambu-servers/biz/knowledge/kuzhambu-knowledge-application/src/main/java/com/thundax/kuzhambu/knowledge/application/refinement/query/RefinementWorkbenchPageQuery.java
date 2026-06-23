package com.thundax.kuzhambu.knowledge.application.refinement.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementWorkbenchPageQuery {
    private String taskType;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String status;
    private int pageNo;
    private int pageSize;
}
