package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "TagContentRefResponse", description = "标签内容引用响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagContentRefResponse implements Serializable {

    @Schema(name = "id", description = "内容引用ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "contentType", description = "内容类型")
    @JsonProperty(value = "contentType")
    private String contentType;

    @Schema(name = "contentId", description = "内容ID")
    @JsonProperty(value = "contentId")
    private String contentId;

    @Schema(name = "contentTitle", description = "内容标题")
    @JsonProperty(value = "contentTitle")
    private String contentTitle;

    @Schema(name = "source", description = "来源")
    @JsonProperty(value = "source")
    private String source;
}
