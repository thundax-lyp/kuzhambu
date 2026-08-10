package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "古籍发布任务创建响应")
public record ClassicsPublicationCreateResponse(
        @Schema(description = "发布任务ID") Long jobId,
        @Schema(description = "内容类型") String contentType,
        @Schema(description = "内容ID") Long contentId,
        @Schema(description = "当前生命周期状态") String lifecycleStatus,
        @Schema(description = "当前发布流转状态") String transitionStatus) {}
