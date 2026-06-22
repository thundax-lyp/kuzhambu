package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TagCategoryResponse", description = "标签分类响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagCategoryResponse implements Serializable {

    @Schema(name = "id", description = "分类ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "name", description = "分类名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "description", description = "分类描述")
    @JsonProperty(value = "description")
    private String description;

    @Schema(name = "priority", description = "分类排序值")
    @JsonProperty(value = "priority")
    private Integer priority;

    @Schema(name = "status", description = "分类状态")
    @JsonProperty(value = "status")
    private String status;
}
