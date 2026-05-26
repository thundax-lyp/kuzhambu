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
@Schema(name = "WecomLoginRequest", description = "企业微信登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WecomLoginRequest implements Serializable {

    @Schema(name = "code", description = "企业微信授权码")
    @JsonProperty(value = "code")
    private String code;
}
