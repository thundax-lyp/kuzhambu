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
@Schema(name = "TagReviewPageRequest", description = "待审核标签分页查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagReviewPageRequest extends PageRequest {

    @Schema(name = "name", description = "标签名称，模糊查询")
    @JsonProperty(value = "name")
    @Size(max = 128, message = "\"标签名称\"长度不能超过128")
    private String name;

    @Schema(name = "source", description = "标签来源")
    @JsonProperty(value = "source")
    @Size(max = 32, message = "\"来源\"长度不能超过32")
    private String source;

    @Schema(name = "sortDirection", description = "排序方向")
    @JsonProperty(value = "sortDirection")
    @Size(max = 10, message = "\"排序方向\"长度不能超过10")
    private String sortDirection;
}
