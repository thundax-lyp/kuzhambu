package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "古籍发布任务响应")
public record ClassicsPublicationJobResponse(
        @Schema(description = "发布任务ID") Long id,
        @Schema(description = "发布任务类型") String jobType,
        @Schema(description = "发布任务执行状态") String jobStatus,
        @Schema(description = "发布任务结果状态") String jobResultStatus,
        @Schema(description = "失败步骤") String failureStep,
        @Schema(description = "内容类型") String contentType,
        @Schema(description = "内容ID") Long contentId,
        @Schema(description = "内容标题快照") String contentTitleSnapshot,
        @Schema(description = "内容删除时间") Instant contentDeletedAt,
        @Schema(description = "源生命周期状态") String sourceLifecycleStatus,
        @Schema(description = "目标生命周期状态") String targetLifecycleStatus,
        @Schema(description = "内容版本ID") Long contentVersionId,
        @Schema(description = "内容版本号") Integer contentVersionNo,
        @Schema(description = "当前尝试次数") Integer attemptCount,
        @Schema(description = "最大尝试次数") Integer maxAttempts,
        @Schema(description = "执行租约过期时间") Instant expiresAt,
        @Schema(description = "下次重试时间") Instant nextRetryAt,
        @Schema(description = "搜索文档ID") String esDocumentId,
        @Schema(description = "搜索清理状态") String esCleanupStatus,
        @Schema(description = "FastGPT 集合ID") String fastgptCollectionId,
        @Schema(description = "FastGPT 清理状态") String fastgptCleanupStatus,
        @Schema(description = "失败原因") String failureReason,
        @Schema(description = "详情摘要JSON") String detailJsonSummary,
        @Schema(description = "请求时间") Instant requestedAt,
        @Schema(description = "开始时间") Instant startedAt,
        @Schema(description = "完成时间") Instant finishedAt) {}
