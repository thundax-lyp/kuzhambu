package com.thundax.kuzhambu.interfaces.admin.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "TokenRefreshRequest", description = "Token 刷新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRefreshRequest implements Serializable {

    @Schema(name = "clientId", description = "客户端标识")
    @JsonProperty(value = "clientId")
    private String clientId;

    @Schema(name = "refreshToken", description = "refresh token")
    @JsonProperty(value = "refreshToken")
    private String refreshToken;
}
