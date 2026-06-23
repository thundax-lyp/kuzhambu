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
@Schema(name = "TagDeprecateRequest", description = "标签废弃请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagDeprecateRequest {

    @Schema(name = "id", description = "标签ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "\"标签ID\"不能为空")
    @Size(max = 64, message = "\"标签ID\"长度不能超过64")
    private String id;
}
