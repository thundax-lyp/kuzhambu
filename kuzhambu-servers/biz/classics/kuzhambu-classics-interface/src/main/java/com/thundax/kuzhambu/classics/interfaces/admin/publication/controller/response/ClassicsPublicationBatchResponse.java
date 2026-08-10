package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "古籍发布批量操作响应")
public record ClassicsPublicationBatchResponse(
        @Schema(description = "受理数量") long acceptedCount,
        @Schema(description = "拒绝数量") long rejectedCount,
        @Schema(description = "结果项列表") List<ClassicsPublicationBatchItemResponse> items) {}
