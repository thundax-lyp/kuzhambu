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
@Schema(name = "OAuth2TokenRequest", description = "OAuth2 token 请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2TokenRequest implements Serializable {
    @JsonProperty(value = "clientId")
    private String clientId;

    @JsonProperty(value = "clientSecret")
    private String clientSecret;

    @JsonProperty(value = "grantType")
    private String grantType;

    @JsonProperty(value = "redirectUri")
    private String redirectUri;

    @JsonProperty(value = "authorizationCode")
    private String authorizationCode;

    @JsonProperty(value = "codeVerifier")
    private String codeVerifier;

    @JsonProperty(value = "refreshToken")
    private String refreshToken;

    @JsonProperty(value = "token")
    private String token;
}
