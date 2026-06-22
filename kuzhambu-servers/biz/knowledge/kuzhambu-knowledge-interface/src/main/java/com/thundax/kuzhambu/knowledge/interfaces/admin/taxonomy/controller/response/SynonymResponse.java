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
@Schema(name = "SynonymResponse", description = "同义词响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynonymResponse implements Serializable {

    @Schema(name = "id", description = "同义词ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "term", description = "词条")
    @JsonProperty(value = "term")
    private String term;

    @Schema(name = "synonym", description = "同义词")
    @JsonProperty(value = "synonym")
    private String synonym;

    @Schema(name = "status", description = "同义词状态")
    @JsonProperty(value = "status")
    private String status;
}
