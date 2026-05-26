package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DictQueryRequest", description = "字典查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictQueryRequest implements Serializable {

    @Schema(name = "label", description = "标签")
    @JsonProperty(value = "label")
    private String label;

    @Schema(name = "type", description = "类型")
    @JsonProperty(value = "type")
    private String type;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;
}
