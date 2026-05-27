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
@Schema(name = "AuthLoginFormRefreshRequest", description = "登录表单刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginFormRefreshRequest implements Serializable {

    @Schema(name = "refreshToken", description = "刷新令牌")
    @JsonProperty(value = "refreshToken")
    @NotEmpty(message = "\"refreshToken\"不能为空")
    private String refreshToken;
}
