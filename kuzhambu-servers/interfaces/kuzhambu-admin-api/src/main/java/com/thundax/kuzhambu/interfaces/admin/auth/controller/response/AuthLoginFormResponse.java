package com.thundax.kuzhambu.interfaces.admin.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuthLoginFormResponse", description = "登录表单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLoginFormResponse implements Serializable {

    @Schema(name = "loginToken", description = "登录令牌")
    @JsonProperty(value = "loginToken")
    private String loginToken;

    @Schema(name = "refreshToken", description = "刷新令牌")
    @JsonProperty(value = "refreshToken")
    private String refreshToken;

    @Schema(name = "expiredAt", description = "过期时间戳，单位：毫秒。")
    @JsonProperty(value = "expiredAt")
    private Long expiredAt;

    @Schema(name = "publicKey", description = "公钥")
    @JsonProperty(value = "publicKey")
    private String publicKey;
}
