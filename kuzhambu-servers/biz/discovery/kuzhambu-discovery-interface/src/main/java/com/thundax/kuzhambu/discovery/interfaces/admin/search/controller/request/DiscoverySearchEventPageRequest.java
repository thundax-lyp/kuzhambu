package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DiscoverySearchEventPageRequest", description = "Discovery 检索统计事件分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchEventPageRequest extends PageRequest {

    @Schema(name = "queryText", description = "搜索词筛选")
    @JsonProperty(value = "queryText")
    private String queryText;

    @Schema(name = "intentTypes", description = "意图筛选")
    @JsonProperty(value = "intentTypes")
    private List<String> intentTypes;

    @Schema(name = "searchStatuses", description = "状态筛选")
    @JsonProperty(value = "searchStatuses")
    private List<String> searchStatuses;

    @Schema(name = "operatorId", description = "操作者筛选")
    @JsonProperty(value = "operatorId")
    private String operatorId;

    @Schema(name = "dateFrom", description = "ISO-8601 起始时间")
    @JsonProperty(value = "dateFrom")
    private String dateFrom;

    @Schema(name = "dateTo", description = "ISO-8601 结束时间")
    @JsonProperty(value = "dateTo")
    private String dateTo;
}
