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
@Schema(name = "DepartmentResponse", description = "部门响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentResponse implements Serializable {

    @Schema(name = "id", description = "部门ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "shortName", description = "简称")
    @JsonProperty(value = "shortName")
    private String shortName;

    @Schema(name = "namePath", description = "全名称")
    @JsonProperty(value = "namePath")
    private String namePath;
}
