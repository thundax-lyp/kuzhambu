package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "古籍发布批量操作结果项")
public record ClassicsPublicationBatchItemResponse(
        @Schema(description = "内容ID") Long contentId,
        @Schema(description = "是否受理") boolean accepted,
        @Schema(description = "发布任务ID") Long jobId,
        @Schema(description = "失败原因") String reason) {}
