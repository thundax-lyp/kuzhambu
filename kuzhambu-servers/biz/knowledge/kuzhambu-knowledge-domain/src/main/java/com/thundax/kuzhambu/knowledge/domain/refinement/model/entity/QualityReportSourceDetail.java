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
public class QualityReportSourceDetail {
    private Long id;
    private Long detailId;
    private Long reportId;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private Long graphVersionId;
    private Instant appliedAt;
    private Long annotationCount;
    private Long issueCount;
    private String status;
    private String href;
    private Instant createdAt;
}
