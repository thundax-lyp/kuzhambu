package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchStatisticsSummaryRequest", description = "Discovery 检索统计摘要请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchStatisticsSummaryRequest {

    @Schema(name = "dateFrom", description = "ISO-8601 起始时间")
    @JsonProperty(value = "dateFrom")
    private String dateFrom;

    @Schema(name = "dateTo", description = "ISO-8601 结束时间")
    @JsonProperty(value = "dateTo")
    private String dateTo;
}
