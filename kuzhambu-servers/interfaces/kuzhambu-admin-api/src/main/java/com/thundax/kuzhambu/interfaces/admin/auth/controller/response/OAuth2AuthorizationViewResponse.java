package com.thundax.kuzhambu.interfaces.admin.auth.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OAuth2AuthorizationViewResponse", description = "OAuth2 授权视图响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2AuthorizationViewResponse implements Serializable {
    @JsonProperty(value = "clientId")
    private String clientId;

    @JsonProperty(value = "clientName")
    private String clientName;

    @JsonProperty(value = "redirectUri")
    private String redirectUri;

    @JsonProperty(value = "scopes")
    private Set<String> scopes;

    @JsonProperty(value = "state")
    private String state;
}
