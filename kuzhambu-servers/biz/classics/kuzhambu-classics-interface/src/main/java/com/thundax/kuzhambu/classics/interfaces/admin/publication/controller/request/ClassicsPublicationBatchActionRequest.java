package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "古籍发布批量操作请求")
public record ClassicsPublicationBatchActionRequest(
        @Schema(description = "内容ID列表") @NotEmpty List<@Valid @NotNull Long> ids) {}
