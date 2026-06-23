package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagGovernanceMetricsRequest", description = "标签治理统计请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagGovernanceMetricsRequest {

    @Schema(name = "topLimit", description = "使用排行返回条数")
    @JsonProperty(value = "topLimit")
    @Min(value = 1, message = "\"topLimit\"最小为1")
    @Max(value = 100, message = "\"topLimit\"最大为100")
    private Integer topLimit;

    @Schema(name = "recentMonths", description = "月度新增趋势返回月数")
    @JsonProperty(value = "recentMonths")
    @Min(value = 1, message = "\"recentMonths\"最小为1")
    @Max(value = 24, message = "\"recentMonths\"最大为24")
    private Integer recentMonths;
}
