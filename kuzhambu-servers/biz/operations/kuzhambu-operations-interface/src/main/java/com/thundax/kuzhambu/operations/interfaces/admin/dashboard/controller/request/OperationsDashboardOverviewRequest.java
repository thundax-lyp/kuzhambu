package com.thundax.kuzhambu.operations.interfaces.admin.dashboard.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsDashboardOverviewRequest", description = "Operations 运营看板概览请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsDashboardOverviewRequest {

    @Schema(name = "periodType", description = "统计周期：WEEK、MONTH、CUSTOM")
    @JsonProperty(value = "periodType")
    private String periodType;

    @Schema(name = "periodStart", description = "自定义统计起始时间")
    @JsonProperty(value = "periodStart")
    private Date periodStart;

    @Schema(name = "periodEnd", description = "自定义统计结束时间")
    @JsonProperty(value = "periodEnd")
    private Date periodEnd;
}
