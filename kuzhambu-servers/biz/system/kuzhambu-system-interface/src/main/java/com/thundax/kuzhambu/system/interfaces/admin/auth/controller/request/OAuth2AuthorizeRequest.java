package com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OAuth2AuthorizeRequest", description = "OAuth2 授权请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2AuthorizeRequest implements Serializable {
    @JsonProperty(value = "clientId")
    private String clientId;

    @JsonProperty(value = "redirectUri")
    private String redirectUri;

    @JsonProperty(value = "scopes")
    private List<String> scopes;

    @JsonProperty(value = "state")
    private String state;

    @JsonProperty(value = "codeChallenge")
    private String codeChallenge;

    @JsonProperty(value = "codeChallengeMethod")
    private String codeChallengeMethod;
}
