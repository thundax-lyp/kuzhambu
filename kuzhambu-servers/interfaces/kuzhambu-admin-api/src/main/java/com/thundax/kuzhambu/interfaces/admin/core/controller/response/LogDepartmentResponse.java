package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "LogDepartmentResponse", description = "日志部门摘要响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogDepartmentResponse implements Serializable {

    @Schema(name = "id", description = "部门ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "namePath", description = "全名称")
    @JsonProperty(value = "namePath")
    private String namePath;
}
