package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "UserResponse", description = "用户响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse implements Serializable {

    @Schema(name = "id", description = "用户ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "loginName", description = "登录名")
    @JsonProperty(value = "loginName")
    private String loginName;

    @Schema(name = "ranks", description = "等级")
    @JsonProperty(value = "ranks")
    private Integer ranks;

    @Schema(name = "name", description = "姓名")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "email", description = "邮箱")
    @JsonProperty(value = "email")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    @JsonProperty(value = "mobile")
    private String mobile;

    @Schema(name = "avatar", description = "头像链接地址")
    @JsonProperty(value = "avatar")
    private String avatar;

    @Schema(name = "superAdmin", description = "是否系统管理员")
    @JsonProperty(value = "superAdmin")
    private Boolean superAdmin;

    @Schema(name = "admin", description = "是否管理员")
    @JsonProperty(value = "admin")
    private Boolean admin;

    @Schema(name = "enable", description = "启用/禁用")
    @JsonProperty(value = "enable")
    private Boolean enable;

    @Schema(name = "department", description = "归属部门")
    @JsonProperty(value = "department")
    private UserDepartmentResponse department;

    @Schema(name = "roles", description = "权限")
    @JsonProperty(value = "roles")
    private List<UserRoleResponse> roleList;
}
