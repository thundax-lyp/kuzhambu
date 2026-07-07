package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagBatchMergeRequest", description = "标签批量合并请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagBatchMergeRequest {

    @Schema(name = "sourceTagIds", description = "源标签ID列表")
    @JsonProperty(value = "sourceTagIds")
    @NotEmpty(message = "\"源标签ID列表\"不能为空")
    private List<@Size(max = 64, message = "\"源标签ID\"长度不能超过64") String> sourceTagIds;

    @Schema(name = "targetTagId", description = "目标标签ID")
    @JsonProperty(value = "targetTagId")
    @NotEmpty(message = "\"目标标签ID\"不能为空")
    @Size(max = 64, message = "\"目标标签ID\"长度不能超过64")
    private String targetTagId;
}
