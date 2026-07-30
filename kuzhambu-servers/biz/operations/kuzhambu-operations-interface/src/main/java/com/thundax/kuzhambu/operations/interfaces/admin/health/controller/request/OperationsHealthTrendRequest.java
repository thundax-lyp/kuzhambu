package com.thundax.kuzhambu.operations.interfaces.admin.health.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsHealthTrendRequest", description = "Operations 健康趋势请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsHealthTrendRequest {

    @Schema(name = "component", description = "组件筛选")
    @JsonProperty(value = "component")
    private String component;

    @Schema(name = "probeSource", description = "采集来源筛选")
    @JsonProperty(value = "probeSource")
    private String probeSource;

    @Schema(name = "periodStart", description = "统计起始时间")
    @JsonProperty(value = "periodStart")
    private Instant periodStart;

    @Schema(name = "periodEnd", description = "统计结束时间")
    @JsonProperty(value = "periodEnd")
    private Instant periodEnd;

    @Schema(name = "bucketType", description = "趋势粒度：HOUR、DAY")
    @JsonProperty(value = "bucketType")
    private String bucketType;
}
