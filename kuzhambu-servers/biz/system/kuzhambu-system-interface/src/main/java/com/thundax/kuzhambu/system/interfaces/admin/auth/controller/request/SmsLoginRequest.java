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
@Schema(name = "SmsLoginRequest", description = "短信登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsLoginRequest implements Serializable {

    @Schema(name = "loginToken", description = "登录 token")
    @JsonProperty(value = "loginToken")
    @NotEmpty(message = "token不能为空")
    private String loginToken;

    @Schema(name = "mobile", description = "手机号")
    @JsonProperty(value = "mobile")
    @NotEmpty(message = "手机号不能为空")
    private String mobile;

    @Schema(name = "validateCode", description = "短信验证码")
    @JsonProperty(value = "validateCode")
    @NotEmpty(message = "短信验证码不能为空")
    private String validateCode;
}
