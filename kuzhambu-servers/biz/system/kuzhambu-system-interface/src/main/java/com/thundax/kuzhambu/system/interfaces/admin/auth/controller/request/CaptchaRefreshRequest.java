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
@Schema(name = "CaptchaRefreshRequest", description = "图形验证码刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptchaRefreshRequest implements Serializable {

    @Schema(name = "loginToken", description = "登录表单token")
    @JsonProperty(value = "loginToken")
    @NotEmpty(message = "\"loginToken\"不能为空")
    private String loginToken;
}
