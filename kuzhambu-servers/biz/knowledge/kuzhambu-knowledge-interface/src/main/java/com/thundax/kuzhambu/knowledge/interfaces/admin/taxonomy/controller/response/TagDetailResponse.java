package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

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
@Schema(name = "TagDetailResponse", description = "标签详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagDetailResponse implements Serializable {

    @Schema(name = "tag", description = "标签基本信息")
    @JsonProperty(value = "tag")
    private TagResponse tag;

    @Schema(name = "aliases", description = "标签别名列表")
    @JsonProperty(value = "aliases")
    private List<TagAliasResponse> aliases;

    @Schema(name = "contentRefs", description = "标签内容引用列表")
    @JsonProperty(value = "contentRefs")
    private List<TagContentRefResponse> contentRefs;
}
