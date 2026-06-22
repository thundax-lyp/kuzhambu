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
@Schema(name = "SynonymCreateRequest", description = "同义词创建请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynonymCreateRequest {

    @Schema(name = "id", description = "同义词ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "term", description = "同义词项")
    @JsonProperty(value = "term")
    @NotEmpty(message = "\"同义词项\"不能为空")
    @Size(max = 128, message = "\"同义词项\"长度不能超过128")
    private String term;

    @Schema(name = "synonym", description = "同义词")
    @JsonProperty(value = "synonym")
    @NotEmpty(message = "\"同义词\"不能为空")
    @Size(max = 128, message = "\"同义词\"长度不能超过128")
    private String synonym;

    @Schema(name = "status", description = "同义词状态")
    @JsonProperty(value = "status")
    @Size(max = 32, message = "\"同义词状态\"长度不能超过32")
    private String status;
}
