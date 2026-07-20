package com.thundax.kuzhambu.discovery.interfaces.admin.search.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "DiscoverySearchEventDetailResponse", description = "Discovery 检索统计事件详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchEventDetailResponse implements Serializable {

    @Schema(name = "searchEventId", description = "检索统计事件号")
    @JsonProperty(value = "searchEventId")
    private String searchEventId;

    @Schema(name = "queryText", description = "原始搜索词")
    @JsonProperty(value = "queryText")
    private String queryText;

    @Schema(name = "normalizedQueryText", description = "清洗后的搜索词")
    @JsonProperty(value = "normalizedQueryText")
    private String normalizedQueryText;

    @Schema(name = "displayQueryText", description = "回显搜索词")
    @JsonProperty(value = "displayQueryText")
    private String displayQueryText;

    @Schema(name = "intentType", description = "意图")
    @JsonProperty(value = "intentType")
    private String intentType;

    @Schema(name = "searchScopesJson", description = "检索范围 JSON")
    @JsonProperty(value = "searchScopesJson")
    private String searchScopesJson;

    @Schema(name = "resultTotalCount", description = "总结果数")
    @JsonProperty(value = "resultTotalCount")
    private Integer resultTotalCount;

    @Schema(name = "groupTotalCount", description = "分组数")
    @JsonProperty(value = "groupTotalCount")
    private Integer groupTotalCount;

    @Schema(name = "searchStatus", description = "搜索状态")
    @JsonProperty(value = "searchStatus")
    private String searchStatus;

    @Schema(name = "failureCode", description = "失败码")
    @JsonProperty(value = "failureCode")
    private String failureCode;

    @Schema(name = "failureMessage", description = "失败摘要")
    @JsonProperty(value = "failureMessage")
    private String failureMessage;

    @Schema(name = "operatorId", description = "操作者")
    @JsonProperty(value = "operatorId")
    private String operatorId;

    @Schema(name = "requestId", description = "请求标识")
    @JsonProperty(value = "requestId")
    private String requestId;

    @Schema(name = "traceId", description = "链路标识")
    @JsonProperty(value = "traceId")
    private String traceId;

    @Schema(name = "createdAt", description = "创建时间")
    @JsonProperty(value = "createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
