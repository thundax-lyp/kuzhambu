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
@Schema(name = "LogUserResponse", description = "日志用户摘要响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogUserResponse implements Serializable {

    @Schema(name = "id", description = "用户ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "loginName", description = "登录名")
    @JsonProperty(value = "loginName")
    private String loginName;

    @Schema(name = "name", description = "姓名")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "department", description = "归属部门")
    @JsonProperty(value = "department")
    private LogDepartmentResponse department;
}
