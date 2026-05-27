package com.thundax.kuzhambu.system.interfaces.admin.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuthAccessTokenResponse", description = "访问令牌响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthAccessTokenResponse implements Serializable {

    @Schema(name = "token", description = "令牌")
    @JsonProperty(value = "token")
    private String token;

    @Schema(name = "refreshToken", description = "刷新令牌")
    @JsonProperty(value = "refreshToken")
    private String refreshToken;

    @Schema(name = "expireAt", description = "访问令牌过期时间")
    @JsonProperty(value = "expireAt")
    private Long expireAt;
}
