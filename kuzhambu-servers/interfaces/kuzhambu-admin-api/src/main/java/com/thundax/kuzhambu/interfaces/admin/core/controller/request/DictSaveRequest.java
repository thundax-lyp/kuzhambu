package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DictSaveRequest", description = "字典保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictSaveRequest implements Serializable {

    @Schema(name = "id", description = "字典ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
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
