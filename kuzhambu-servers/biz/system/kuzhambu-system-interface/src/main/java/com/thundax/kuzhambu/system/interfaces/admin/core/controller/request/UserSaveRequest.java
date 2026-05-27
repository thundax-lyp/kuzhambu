package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserSaveRequest", description = "用户保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSaveRequest implements Serializable {

    @Schema(name = "id", description = "用户ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @Schema(name = "loginName", description = "登录名")
    @JsonProperty(value = "loginName")
    @NotEmpty(message = "\"登录名\"不能为空")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    private String loginName;

    @Schema(name = "loginPass", description = "登录密码")
    @JsonProperty(value = "loginPass")
    private String loginPass;

    @Schema(name = "ranks", description = "等级")
    @JsonProperty(value = "ranks")
    @Min(value = 0, message = "等级不能小于0")
    @Max(value = 9, message = "等级不能超过9")
    private Integer ranks;

    @Schema(name = "name", description = "姓名")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"姓名\"不能为空")
    @Size(max = 50, message = "\"姓名\"长度不能超过 50")
    private String name;

    @Schema(name = "email", description = "邮箱")
    @JsonProperty(value = "email")
    @Size(max = 50, message = "\"邮箱\"长度不能超过 50")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    @JsonProperty(value = "mobile")
    @Size(max = 30, message = "\"手机号\"长度不能超过 30")
    private String mobile;

    @Schema(name = "admin", description = "是否管理员")
    @JsonProperty(value = "admin")
    private Boolean admin;

    @Schema(name = "enable", description = "启用/禁用")
    @JsonProperty(value = "enable")
    private Boolean enable;

    @Schema(name = "token", description = "令牌")
    @JsonProperty(value = "token")
    private String token;

    @Schema(name = "department", description = "归属部门")
    @JsonProperty(value = "department")
    private UserDepartmentRequest department;

    @Schema(name = "roles", description = "权限")
    @JsonProperty(value = "roles")
    private List<UserRoleRequest> roleList;
}
