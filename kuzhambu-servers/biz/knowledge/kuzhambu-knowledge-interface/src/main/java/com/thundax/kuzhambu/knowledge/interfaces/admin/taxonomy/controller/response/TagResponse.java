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
@Schema(name = "TagResponse", description = "标签响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagResponse implements Serializable {

    @Schema(name = "id", description = "标签ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "name", description = "标签名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "categoryId", description = "分类ID")
    @JsonProperty(value = "categoryId")
    private String categoryId;

    @Schema(name = "categoryName", description = "分类名称")
    @JsonProperty(value = "categoryName")
    private String categoryName;

    @Schema(name = "description", description = "标签描述")
    @JsonProperty(value = "description")
    private String description;

    @Schema(name = "status", description = "标签状态")
    @JsonProperty(value = "status")
    private String status;

    @Schema(name = "source", description = "标签来源")
    @JsonProperty(value = "source")
    private String source;

    @Schema(name = "reviewStatus", description = "审核状态")
    @JsonProperty(value = "reviewStatus")
    private String reviewStatus;

    @Schema(name = "contentRefCount", description = "内容引用数量")
    @JsonProperty(value = "contentRefCount")
    private Integer contentRefCount;

    @Schema(name = "createdAt", description = "创建时间戳")
    @JsonProperty(value = "createdAt")
    private Long createdAt;

    @Schema(name = "reviewedAt", description = "审核时间戳")
    @JsonProperty(value = "reviewedAt")
    private Long reviewedAt;
}
