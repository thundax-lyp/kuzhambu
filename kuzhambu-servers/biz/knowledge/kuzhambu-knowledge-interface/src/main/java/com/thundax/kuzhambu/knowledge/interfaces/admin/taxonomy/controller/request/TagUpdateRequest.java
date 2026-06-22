package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagUpdateRequest", description = "标签更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagUpdateRequest {

    @Schema(name = "id", description = "标签ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "name", description = "标签名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"标签名称\"不能为空")
    @Size(max = 128, message = "\"标签名称\"长度不能超过128")
    private String name;

    @Schema(name = "categoryId", description = "分类ID")
    @JsonProperty(value = "categoryId")
    @Size(max = 64, message = "\"分类ID\"长度不能超过64")
    private String categoryId;

    @Schema(name = "description", description = "标签描述")
    @JsonProperty(value = "description")
    @Size(max = 1024, message = "\"标签描述\"长度不能超过1024")
    private String description;
}
