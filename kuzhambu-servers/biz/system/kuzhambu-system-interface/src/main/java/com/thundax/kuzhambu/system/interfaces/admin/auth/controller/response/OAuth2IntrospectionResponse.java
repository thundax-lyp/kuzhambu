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
@Schema(name = "OAuth2IntrospectionResponse", description = "OAuth2 token introspection 响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2IntrospectionResponse implements Serializable {

    @Schema(name = "active", description = "是否活跃")
    @JsonProperty(value = "active")
    private boolean active;

    @Schema(name = "sub", description = "用户标识")
    @JsonProperty(value = "sub")
    private String subject;

    @Schema(name = "username", description = "用户名")
    @JsonProperty(value = "username")
    private String username;

    @Schema(name = "client_id", description = "OAuth2 客户端标识")
    @JsonProperty(value = "client_id")
    private String clientId;

    @Schema(name = "scope", description = "OAuth2 授权范围")
    @JsonProperty(value = "scope")
    private String scope;

    @Schema(name = "exp", description = "OAuth2 token 过期时间戳")
    @JsonProperty(value = "exp")
    private Long expiresAt;

    @Schema(name = "token_type", description = "OAuth2 token 类型")
    @JsonProperty(value = "token_type")
    private String tokenType;

    @Schema(name = "sessionId", description = "会话标识")
    @JsonProperty(value = "sessionId")
    private String sessionId;
}
