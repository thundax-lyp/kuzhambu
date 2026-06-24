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
@Schema(name = "DiscoverySearchLogResponse", description = "Discovery 搜索日志响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchLogResponse implements Serializable {

    @Schema(name = "searchLogId", description = "搜索日志号")
    @JsonProperty(value = "searchLogId")
    private String searchLogId;

    @Schema(name = "queryText", description = "原始搜索词")
    @JsonProperty(value = "queryText")
    private String queryText;

    @Schema(name = "displayQueryText", description = "回显搜索词")
    @JsonProperty(value = "displayQueryText")
    private String displayQueryText;

    @Schema(name = "intentType", description = "意图")
    @JsonProperty(value = "intentType")
    private String intentType;

    @Schema(name = "resultTotalCount", description = "总结果数")
    @JsonProperty(value = "resultTotalCount")
    private Integer resultTotalCount;

    @Schema(name = "groupTotalCount", description = "分组数")
    @JsonProperty(value = "groupTotalCount")
    private Integer groupTotalCount;

    @Schema(name = "searchStatus", description = "搜索状态")
    @JsonProperty(value = "searchStatus")
    private String searchStatus;

    @Schema(name = "operatorId", description = "操作者")
    @JsonProperty(value = "operatorId")
    private String operatorId;

    @Schema(name = "createdAt", description = "创建时间")
    @JsonProperty(value = "createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
