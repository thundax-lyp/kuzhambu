package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.request;

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
@Schema(name = "DiscoverySearchRequest", description = "Discovery Portal 搜索请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchRequest extends PageRequest {

    @Schema(name = "queryText", description = "搜索词")
    @JsonProperty(value = "queryText")
    private String queryText;

    @Schema(name = "knowledgeBases", description = "知识库范围")
    @JsonProperty(value = "knowledgeBases")
    private List<String> knowledgeBases;

    @Schema(name = "categoryCodes", description = "门类筛选")
    @JsonProperty(value = "categoryCodes")
    private List<String> categoryCodes;

    @Schema(name = "tagNames", description = "标签筛选")
    @JsonProperty(value = "tagNames")
    private List<String> tagNames;

    @Schema(name = "contentStatuses", description = "状态筛选")
    @JsonProperty(value = "contentStatuses")
    private List<String> contentStatuses;

    @Schema(name = "visibilityScopes", description = "可见性筛选")
    @JsonProperty(value = "visibilityScopes")
    private List<String> visibilityScopes;

    @Schema(name = "dateFrom", description = "ISO-8601 起始时间")
    @JsonProperty(value = "dateFrom")
    private String dateFrom;

    @Schema(name = "dateTo", description = "ISO-8601 结束时间")
    @JsonProperty(value = "dateTo")
    private String dateTo;
}
