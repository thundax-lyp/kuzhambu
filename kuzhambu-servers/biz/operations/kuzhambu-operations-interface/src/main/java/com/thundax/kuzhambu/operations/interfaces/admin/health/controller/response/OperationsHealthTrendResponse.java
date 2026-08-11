package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsHealthTrendResponse", description = "Operations 健康趋势响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthTrendResponse {
    private String bucket;
    private Long upCount;
    private Long degradedCount;
    private Long downCount;
    private Long avgLatencyMs;
}
