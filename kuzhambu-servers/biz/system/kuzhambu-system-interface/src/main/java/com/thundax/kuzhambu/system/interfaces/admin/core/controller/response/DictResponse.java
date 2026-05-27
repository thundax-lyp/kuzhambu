package com.thundax.kuzhambu.system.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "DictResponse", description = "字典响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictResponse implements Serializable {

    @Schema(name = "id", description = "字典ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "type", description = "类型")
    @JsonProperty(value = "type")
    private String type;

    @Schema(name = "label", description = "标签")
    @JsonProperty(value = "label")
    private String label;

    @Schema(name = "value", description = "值")
    @JsonProperty(value = "value")
    private String value;
}
