package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DepartmentSaveRequest", description = "部门保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentSaveRequest implements Serializable {

    @Schema(name = "id", description = "部门ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    private String name;

    @Schema(name = "shortName", description = "简称")
    @JsonProperty(value = "shortName")
    @Size(max = 50, message = "\"简称\"长度不能超过 50")
    private String shortName;
}
