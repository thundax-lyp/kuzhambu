package com.thundax.kuzhambu.knowledge.interfaces.admin.refinement.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class QualityReportResponses {

    private QualityReportResponses() {}

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailResponse {
        private ReportResponse report;
        private List<IssueResponse> issues;
        private List<SourceDetailResponse> sourceDetails;
        private List<AnnotationResponse> annotations;
        private Boolean stale;
        private String staleReason;
        private Long lastRefinementAppliedAt;
    }

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReportResponse {
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
    }

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueResponse {
        private Long issueId;
        private String issueType;
        private String severity;
        private String objectType;
        private String objectKey;
        private String title;
        private String description;
        private String suggestion;
        private String href;
    }

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SourceDetailResponse {
        private Long detailId;
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
    }

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnnotationResponse {
        private Long annotationId;
        private String objectType;
        private String objectKey;
        private Long graphVersionId;
        private String annotationStatus;
        private String annotationLabel;
        private String comment;
    }

    @Getter
    @Builder
    @Schema
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReextractResponse {
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
}
