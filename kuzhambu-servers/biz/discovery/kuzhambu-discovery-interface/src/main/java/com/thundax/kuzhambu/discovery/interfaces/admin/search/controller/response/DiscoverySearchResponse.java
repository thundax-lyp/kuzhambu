package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "DiscoverySearchStatisticsResponse", description = "Discovery 后台搜索响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchResponse implements Serializable {

    @Schema(name = "searchEventId", description = "检索统计事件号")
    @JsonProperty(value = "searchEventId")
    private String searchEventId;

    @Schema(name = "queryText", description = "原始搜索词")
    @JsonProperty(value = "queryText")
    private String queryText;

    @Schema(name = "displayQueryText", description = "前端回显搜索词")
    @JsonProperty(value = "displayQueryText")
    private String displayQueryText;

    @Schema(name = "totalCount", description = "总命中数")
    @JsonProperty(value = "totalCount")
    private Integer totalCount;

    @Schema(name = "groupCount", description = "分组数")
    @JsonProperty(value = "groupCount")
    private Integer groupCount;

    @Schema(name = "groups", description = "分组结果")
    @JsonProperty(value = "groups")
    private List<DiscoverySearchGroupResponse> groups;
}
