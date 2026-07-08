package com.thundax.kuzhambu.knowledge.application.refinement.result;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityReportDetailResult {
    private ReportRecord report;
    private List<IssueRecord> issues;
    private List<SourceDetailRecord> sourceDetails;
    private List<QualityAnnotationResult> annotations;
    private Boolean stale;
    private String staleReason;
    private Long lastRefinementAppliedAt;

    public QualityReportDetailResult(
            ReportRecord report,
            List<IssueRecord> issues,
            List<SourceDetailRecord> sourceDetails,
            List<QualityAnnotationResult> annotations) {
        this(report, issues, sourceDetails, annotations, Boolean.FALSE, null, null);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportRecord {
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
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssueRecord {
        private Long issueId;
        private String issueType;
        private String severity;
        private String objectType;
        private String objectKey;
        private String title;
        private String description;
        private String suggestion;
        private String href;
        private Integer priority;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDetailRecord {
        private Long detailId;
        private String sourceContentType;
        private Long sourceContentId;
        private String sourceCategoryCode;
        private String sourceCategoryName;
        private Long graphVersionId;
        private Date appliedAt;
        private Long annotationCount;
        private Long issueCount;
        private String status;
        private String href;
    }
}
