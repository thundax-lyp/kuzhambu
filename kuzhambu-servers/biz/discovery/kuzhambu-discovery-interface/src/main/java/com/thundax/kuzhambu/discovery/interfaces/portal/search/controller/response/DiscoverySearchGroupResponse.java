package com.thundax.kuzhambu.discovery.interfaces.portal.search.controller.response;

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
@Schema(name = "DiscoverySearchGroupResponse", description = "Discovery Portal 搜索分组响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchGroupResponse implements Serializable {

    @Schema(name = "groupKey", description = "分组键")
    @JsonProperty(value = "groupKey")
    private String groupKey;

    @Schema(name = "groupTitle", description = "分组标题")
    @JsonProperty(value = "groupTitle")
    private String groupTitle;

    @Schema(name = "count", description = "组内命中数")
    @JsonProperty(value = "count")
    private Integer count;

    @Schema(name = "items", description = "组内结果")
    @JsonProperty(value = "items")
    private List<DiscoverySearchItemResponse> items;

    @Getter
    @Builder
    @Schema(name = "DiscoverySearchItemResponse", description = "Discovery Portal 搜索结果项响应")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscoverySearchItemResponse implements Serializable {

        @Schema(name = "contentDomain", description = "内容域")
        @JsonProperty(value = "contentDomain")
        private String contentDomain;

        @Schema(name = "contentType", description = "内容类型")
        @JsonProperty(value = "contentType")
        private String contentType;

        @Schema(name = "contentId", description = "内容业务标识")
        @JsonProperty(value = "contentId")
        private String contentId;

        @Schema(name = "title", description = "标题")
        @JsonProperty(value = "title")
        private String title;

        @Schema(name = "summary", description = "摘要")
        @JsonProperty(value = "summary")
        private String summary;

        @Schema(name = "highlightText", description = "高亮文本")
        @JsonProperty(value = "highlightText")
        private String highlightText;

        @Schema(name = "resultRank", description = "全结果位置")
        @JsonProperty(value = "resultRank")
        private Integer resultRank;

        @Schema(name = "groupRank", description = "组内位置")
        @JsonProperty(value = "groupRank")
        private Integer groupRank;

        @Schema(name = "targetPath", description = "跳转路径")
        @JsonProperty(value = "targetPath")
        private String targetPath;
    }
}
