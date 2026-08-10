package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "古籍发布任务响应")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassicsPublicationJobResponse {
    @Schema(description = "发布任务ID")
    private Long id;

    @Schema(description = "发布任务类型")
    private String jobType;

    @Schema(description = "发布任务执行状态")
    private String jobStatus;

    @Schema(description = "发布任务结果状态")
    private String jobResultStatus;

    @Schema(description = "失败步骤")
    private String failureStep;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "内容ID")
    private Long contentId;

    @Schema(description = "内容标题快照")
    private String contentTitleSnapshot;

    @Schema(description = "内容删除时间")
    private Instant contentDeletedAt;

    @Schema(description = "源生命周期状态")
    private String sourceLifecycleStatus;

    @Schema(description = "目标生命周期状态")
    private String targetLifecycleStatus;

    @Schema(description = "内容版本ID")
    private Long contentVersionId;

    @Schema(description = "内容版本号")
    private Integer contentVersionNo;

    @Schema(description = "当前尝试次数")
    private Integer attemptCount;

    @Schema(description = "最大尝试次数")
    private Integer maxAttempts;

    @Schema(description = "执行租约过期时间")
    private Instant expiresAt;

    @Schema(description = "下次重试时间")
    private Instant nextRetryAt;

    @Schema(description = "搜索文档ID")
    private String esDocumentId;

    @Schema(description = "搜索清理状态")
    private String esCleanupStatus;

    @Schema(description = "FastGPT 集合ID")
    private String fastgptCollectionId;

    @Schema(description = "FastGPT 清理状态")
    private String fastgptCleanupStatus;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "详情摘要JSON")
    private String detailJsonSummary;

    @Schema(description = "请求时间")
    private Instant requestedAt;

    @Schema(description = "开始时间")
    private Instant startedAt;

    @Schema(description = "完成时间")
    private Instant finishedAt;

    public ClassicsPublicationJobResponse(
            Long id,
            String jobType,
            String jobStatus,
            String jobResultStatus,
            String failureStep,
            String contentType,
            Long contentId,
            String contentTitleSnapshot,
            Instant contentDeletedAt,
            String sourceLifecycleStatus,
            String targetLifecycleStatus,
            Long contentVersionId,
            Integer contentVersionNo,
            Integer attemptCount,
            Integer maxAttempts,
            Instant expiresAt,
            Instant nextRetryAt,
            String esDocumentId,
            String esCleanupStatus,
            String fastgptCollectionId,
            String fastgptCleanupStatus,
            String failureReason,
            String detailJsonSummary,
            Instant requestedAt,
            Instant startedAt,
            Instant finishedAt) {
        this.id = id;
        this.jobType = jobType;
        this.jobStatus = jobStatus;
        this.jobResultStatus = jobResultStatus;
        this.failureStep = failureStep;
        this.contentType = contentType;
        this.contentId = contentId;
        this.contentTitleSnapshot = contentTitleSnapshot;
        this.contentDeletedAt = contentDeletedAt;
        this.sourceLifecycleStatus = sourceLifecycleStatus;
        this.targetLifecycleStatus = targetLifecycleStatus;
        this.contentVersionId = contentVersionId;
        this.contentVersionNo = contentVersionNo;
        this.attemptCount = attemptCount;
        this.maxAttempts = maxAttempts;
        this.expiresAt = expiresAt;
        this.nextRetryAt = nextRetryAt;
        this.esDocumentId = esDocumentId;
        this.esCleanupStatus = esCleanupStatus;
        this.fastgptCollectionId = fastgptCollectionId;
        this.fastgptCleanupStatus = fastgptCleanupStatus;
        this.failureReason = failureReason;
        this.detailJsonSummary = detailJsonSummary;
        this.requestedAt = requestedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public Long id() {
        return id;
    }

    public String jobType() {
        return jobType;
    }

    public String jobStatus() {
        return jobStatus;
    }

    public String jobResultStatus() {
        return jobResultStatus;
    }

    public String failureStep() {
        return failureStep;
    }

    public String contentType() {
        return contentType;
    }

    public Long contentId() {
        return contentId;
    }

    public String contentTitleSnapshot() {
        return contentTitleSnapshot;
    }

    public Instant contentDeletedAt() {
        return contentDeletedAt;
    }

    public String sourceLifecycleStatus() {
        return sourceLifecycleStatus;
    }

    public String targetLifecycleStatus() {
        return targetLifecycleStatus;
    }

    public Long contentVersionId() {
        return contentVersionId;
    }

    public Integer contentVersionNo() {
        return contentVersionNo;
    }

    public Integer attemptCount() {
        return attemptCount;
    }

    public Integer maxAttempts() {
        return maxAttempts;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant nextRetryAt() {
        return nextRetryAt;
    }

    public String esDocumentId() {
        return esDocumentId;
    }

    public String esCleanupStatus() {
        return esCleanupStatus;
    }

    public String fastgptCollectionId() {
        return fastgptCollectionId;
    }

    public String fastgptCleanupStatus() {
        return fastgptCleanupStatus;
    }

    public String failureReason() {
        return failureReason;
    }

    public String detailJsonSummary() {
        return detailJsonSummary;
    }

    public Instant requestedAt() {
        return requestedAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }
}
