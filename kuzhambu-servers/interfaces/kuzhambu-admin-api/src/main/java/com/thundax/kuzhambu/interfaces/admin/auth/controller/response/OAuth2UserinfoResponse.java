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
@Schema(name = "OAuth2UserinfoResponse", description = "OAuth2 userinfo 响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuth2UserinfoResponse implements Serializable {

    @Schema(name = "sub", description = "用户标识")
    @JsonProperty(value = "sub")
    private String subject;

    @Schema(name = "username", description = "用户名")
    @JsonProperty(value = "username")
    private String username;

    @Schema(name = "preferred_username", description = "首选用户名")
    @JsonProperty(value = "preferred_username")
    private String preferredUsername;

    @Schema(name = "name", description = "用户名称")
    @JsonProperty(value = "name")
    private String name;
}
