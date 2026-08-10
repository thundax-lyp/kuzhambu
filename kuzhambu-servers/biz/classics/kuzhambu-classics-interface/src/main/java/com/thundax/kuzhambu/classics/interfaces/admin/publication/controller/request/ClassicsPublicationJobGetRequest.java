package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "古籍发布任务详情请求")
public record ClassicsPublicationJobGetRequest(@Schema(description = "发布任务ID") @NotNull Long id) {}
