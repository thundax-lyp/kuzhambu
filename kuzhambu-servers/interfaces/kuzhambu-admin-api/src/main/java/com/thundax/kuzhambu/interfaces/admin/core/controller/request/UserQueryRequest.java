package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

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
@Schema(name = "UserQueryRequest", description = "用户查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQueryRequest extends PageRequest {

    @Schema(name = "departmentId", description = "部门ID")
    @JsonProperty(value = "departmentId")
    @Size(max = 64, message = "\"部门ID\"长度不能超过64")
    private String departmentId;

    @Schema(name = "loginName", description = "登录名，模糊查询")
    @JsonProperty(value = "loginName")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    private String loginName;

    @Schema(name = "name", description = "姓名，模糊查询")
    @JsonProperty(value = "name")
    @Size(max = 30, message = "\"姓名\"长度不能超过 30")
    private String name;

    @Schema(name = "enable", description = "启用/禁用")
    @JsonProperty(value = "enable")
    private Boolean enable;

    @Schema(name = "orderBy", description = "排序规则")
    @JsonProperty(value = "orderBy")
    private String orderBy;
}
