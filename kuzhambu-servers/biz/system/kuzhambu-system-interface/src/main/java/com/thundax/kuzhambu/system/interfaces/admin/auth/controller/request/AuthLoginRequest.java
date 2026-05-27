package com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuthLoginRequest", description = "用户名密码登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginRequest implements Serializable {

    @Schema(name = "loginToken", description = "登录表单token")
    @JsonProperty(value = "loginToken")
    @NotEmpty(message = "token不能为空")
    private String loginToken;

    @Schema(name = "userName", description = "用户名")
    @JsonProperty(value = "userName")
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @Schema(name = "password", description = "用户密码")
    @JsonProperty(value = "password")
    @NotEmpty(message = "用户密码不能为空")
    private String password;

    @Schema(name = "captcha", description = "验证码")
    @JsonProperty(value = "captcha")
    private String captcha;
}
