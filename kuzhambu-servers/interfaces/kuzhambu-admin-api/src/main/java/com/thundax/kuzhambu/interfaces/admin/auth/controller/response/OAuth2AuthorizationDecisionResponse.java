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
@Schema(name = "OAuth2AuthorizationDecisionResponse", description = "OAuth2 授权决策响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2AuthorizationDecisionResponse implements Serializable {
    @JsonProperty(value = "approved")
    private boolean approved;

    @JsonProperty(value = "authorizationCode")
    private String authorizationCode;

    @JsonProperty(value = "state")
    private String state;
}
