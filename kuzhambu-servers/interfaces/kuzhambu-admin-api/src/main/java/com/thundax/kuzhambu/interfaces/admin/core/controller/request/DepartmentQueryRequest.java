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
@Schema(name = "DepartmentQueryRequest", description = "部门查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentQueryRequest implements Serializable {

    @Schema(name = "parentId", description = "父节点ID，为空则查询跟节点")
    @JsonProperty(value = "parentId")
    @Size(max = 64, message = "父节点ID长度不能超过64")
    private String parentId;

    @Schema(name = "name", description = "名称，模糊匹配名称和简称")
    @JsonProperty(value = "name")
    @Size(max = 50, message = "名称长度不能超过50")
    private String name;

    @Schema(name = "remarks", description = "备注，模糊匹配")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "名称长度不能超过200")
    private String remarks;
}
