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
@Schema(name = "PersonalInfoResponse", description = "当前用户信息响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalInfoResponse implements Serializable {

    @Schema(name = "id", description = "用户ID")
    @JsonProperty(value = "id")
    private String id;

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

    @Schema(name = "admin", description = "是否管理员")
    @JsonProperty(value = "admin")
    private Boolean admin;

    @Schema(name = "superAdmin", description = "是否系统管理员")
    @JsonProperty(value = "superAdmin")
    private Boolean superAdmin;
}
