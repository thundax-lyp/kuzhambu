package com.thundax.kuzhambu.knowledge.application.refinement.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityReportPageQuery {
    private Long graphVersionId;
    private String sourceContentType;
    private Long sourceContentId;
    private String reportStatus;
    private int pageNo;
    private int pageSize;
}
