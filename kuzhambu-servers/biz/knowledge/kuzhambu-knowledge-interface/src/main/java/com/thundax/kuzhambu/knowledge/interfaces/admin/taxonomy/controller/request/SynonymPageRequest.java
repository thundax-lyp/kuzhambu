package com.thundax.kuzhambu.knowledge.interfaces.admin.taxonomy.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "SynonymPageRequest", description = "同义词分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynonymPageRequest extends PageRequest {

    @Schema(name = "term", description = "同义词项")
    @JsonProperty(value = "term")
    @Size(max = 128, message = "\"同义词项\"长度不能超过128")
    private String term;

    @Schema(name = "synonym", description = "同义词")
    @JsonProperty(value = "synonym")
    @Size(max = 128, message = "\"同义词\"长度不能超过128")
    private String synonym;

    @Schema(name = "status", description = "同义词状态")
    @JsonProperty(value = "status")
    @Size(max = 32, message = "\"同义词状态\"长度不能超过32")
    private String status;

    @Schema(name = "sortDirection", description = "排序方向")
    @JsonProperty(value = "sortDirection")
    @Size(max = 10, message = "\"排序方向\"长度不能超过10")
    private String sortDirection;
}
