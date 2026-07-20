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
@Schema(name = "TagCategoryUpdateRequest", description = "标签分类更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCategoryUpdateRequest {

    @Schema(name = "id", description = "分类ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "name", description = "分类名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"分类名称\"不能为空")
    @Size(max = 128, message = "\"分类名称\"长度不能超过128")
    private String name;

    @Schema(name = "description", description = "分类描述")
    @JsonProperty(value = "description")
    @Size(max = 512, message = "\"分类描述\"长度不能超过512")
    private String description;
}
