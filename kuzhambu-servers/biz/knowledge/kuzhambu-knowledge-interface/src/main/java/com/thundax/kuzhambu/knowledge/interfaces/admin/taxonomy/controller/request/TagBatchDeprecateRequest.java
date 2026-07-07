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
@Schema(name = "TagBatchDeprecateRequest", description = "标签批量废弃请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagBatchDeprecateRequest {

    @Schema(name = "tagIds", description = "标签ID列表")
    @JsonProperty(value = "tagIds")
    @NotEmpty(message = "\"标签ID列表\"不能为空")
    private List<@Size(max = 64, message = "\"标签ID\"长度不能超过64") String> tagIds;
}
