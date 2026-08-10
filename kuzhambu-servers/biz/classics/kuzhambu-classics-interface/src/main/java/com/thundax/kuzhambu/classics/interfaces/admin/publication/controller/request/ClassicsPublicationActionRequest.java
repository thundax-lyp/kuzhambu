package com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "古籍发布操作请求")
public record ClassicsPublicationActionRequest(@Schema(description = "内容ID") @NotNull Long id) {}
