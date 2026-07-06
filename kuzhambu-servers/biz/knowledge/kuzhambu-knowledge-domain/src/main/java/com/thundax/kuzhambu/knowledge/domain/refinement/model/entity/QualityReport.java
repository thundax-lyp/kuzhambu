package com.thundax.kuzhambu.knowledge.domain.refinement.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityReport {
    private Long id;
    private Long reportId;
    private String reportNo;
    private Long graphVersionId;
    private String sourceContentType;
    private Long sourceContentId;
    private String sourceCategoryCode;
    private String sourceCategoryName;
    private String reportStatus;
    private Long entityTotalCount;
    private Long entityConfirmedCount;
    private Long relationTotalCount;
    private Long relationConfirmedCount;
    private Long lineageTotalCount;
    private Long lineageConfirmedCount;
    private BigDecimal entityCoverageRate;
    private BigDecimal relationAccuracyRate;
    private BigDecimal lineageCoverageRate;
    private BigDecimal completenessRate;
    private Long annotationCount;
    private Long issueCount;
    private Long generatedBy;
    private Date generatedAt;
    private Date publishedAt;
    private Date createdAt;
    private Date updatedAt;
}
