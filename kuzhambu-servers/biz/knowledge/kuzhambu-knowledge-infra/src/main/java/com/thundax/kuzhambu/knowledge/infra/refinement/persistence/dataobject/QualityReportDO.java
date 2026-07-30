package com.thundax.kuzhambu.knowledge.infra.refinement.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_quality_report")
public class QualityReportDO {
    @TableId(type = IdType.INPUT)
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
    private Instant generatedAt;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
