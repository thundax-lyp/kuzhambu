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
@Schema(name = "TagAliasCreateRequest", description = "标签别名创建请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagAliasCreateRequest {

    @Schema(name = "id", description = "别名ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "tagId", description = "标签ID")
    @JsonProperty(value = "tagId")
    @NotEmpty(message = "\"标签ID\"不能为空")
    @Size(max = 64, message = "\"标签ID\"长度不能超过64")
    private String tagId;

    @Schema(name = "name", description = "别名名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"别名\"不能为空")
    @Size(max = 128, message = "\"别名\"长度不能超过128")
    private String name;

    @Schema(name = "source", description = "别名来源")
    @JsonProperty(value = "source")
    @Size(max = 32, message = "\"来源\"长度不能超过32")
    private String source;
}
