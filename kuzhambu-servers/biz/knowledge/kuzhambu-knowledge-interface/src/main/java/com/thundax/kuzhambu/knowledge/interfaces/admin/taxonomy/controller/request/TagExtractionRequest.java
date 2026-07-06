package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagExtractionRequest", description = "AI 标签抽取请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagExtractionRequest {

    @Schema(name = "sourceContentType", description = "来源内容类型")
    @JsonProperty(value = "sourceContentType")
    @NotBlank(message = "\"来源内容类型\"不能为空")
    private String sourceContentType;

    @Schema(name = "sourceContentId", description = "来源内容ID")
    @JsonProperty(value = "sourceContentId")
    @NotNull(message = "\"来源内容ID\"不能为空")
    private Long sourceContentId;

    @Schema(name = "contentTitle", description = "内容标题")
    @JsonProperty(value = "contentTitle")
    private String contentTitle;

    @Schema(name = "contentText", description = "内容片段")
    @JsonProperty(value = "contentText")
    @NotBlank(message = "\"内容片段\"不能为空")
    private String contentText;

    @Schema(name = "modelId", description = "模型ID")
    @JsonProperty(value = "modelId")
    @NotNull(message = "\"模型ID\"不能为空")
    private Long modelId;

    @Schema(name = "modelName", description = "模型名称")
    @JsonProperty(value = "modelName")
    @NotBlank(message = "\"模型名称\"不能为空")
    private String modelName;

    @Schema(name = "promptVersionId", description = "提示词版本ID")
    @JsonProperty(value = "promptVersionId")
    private Long promptVersionId;

    @Schema(name = "maxTags", description = "最大标签数")
    @JsonProperty(value = "maxTags")
    @Min(value = 1, message = "\"最大标签数\"不能小于1")
    private Integer maxTags;

    @Schema(name = "allowNewTags", description = "是否允许创建新标签")
    @JsonProperty(value = "allowNewTags")
    private Boolean allowNewTags;

    @Schema(name = "requestedBy", description = "请求人")
    @JsonProperty(value = "requestedBy")
    @NotNull(message = "\"请求人\"不能为空")
    private Long requestedBy;
}
