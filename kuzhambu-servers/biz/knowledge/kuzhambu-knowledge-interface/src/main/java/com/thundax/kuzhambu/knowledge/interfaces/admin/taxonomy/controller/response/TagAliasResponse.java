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
@Schema(name = "TagAliasResponse", description = "标签别名响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagAliasResponse implements Serializable {

    @Schema(name = "id", description = "别名ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "name", description = "别名")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "source", description = "来源")
    @JsonProperty(value = "source")
    private String source;
}
