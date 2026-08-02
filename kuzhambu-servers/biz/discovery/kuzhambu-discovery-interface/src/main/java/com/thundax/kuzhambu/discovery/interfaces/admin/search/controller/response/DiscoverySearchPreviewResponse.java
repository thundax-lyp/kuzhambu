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
@Schema(name = "DiscoverySearchStatisticsPreviewResponse", description = "Discovery 后台搜索预览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscoverySearchPreviewResponse implements Serializable {

    @Schema(name = "contentDomain", description = "内容域")
    @JsonProperty(value = "contentDomain")
    private String contentDomain;

    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    private String contentType;

    @Schema(name = "contentId", description = "内容 ID")
    @JsonProperty(value = "contentId")
    private String contentId;

    @Schema(name = "knowledgeBase", description = "知识库")
    @JsonProperty(value = "knowledgeBase")
    private String knowledgeBase;

    @Schema(name = "categoryCode", description = "门类编码")
    @JsonProperty(value = "categoryCode")
    private String categoryCode;

    @Schema(name = "categoryName", description = "门类名称")
    @JsonProperty(value = "categoryName")
    private String categoryName;

    @Schema(name = "title", description = "标题")
    @JsonProperty(value = "title")
    private String title;

    @Schema(name = "summary", description = "摘要")
    @JsonProperty(value = "summary")
    private String summary;

    @Schema(name = "bodyText", description = "索引正文")
    @JsonProperty(value = "bodyText")
    private String bodyText;

    @Schema(name = "tagNames", description = "标签")
    @JsonProperty(value = "tagNames")
    private List<String> tagNames;

    @Schema(name = "sourceVersionNo", description = "来源版本号")
    @JsonProperty(value = "sourceVersionNo")
    private Integer sourceVersionNo;

    @Schema(name = "publishedAt", description = "发布时间戳")
    @JsonProperty(value = "publishedAt")
    private Long publishedAt;

    @Schema(name = "updatedAt", description = "更新时间戳")
    @JsonProperty(value = "updatedAt")
    private Long updatedAt;

    @Schema(name = "targetPath", description = "原内容详情路径")
    @JsonProperty(value = "targetPath")
    private String targetPath;
}
