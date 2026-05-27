package com.thundax.kuzhambu.system.interfaces.admin.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuthLogoutRequest", description = "退出登录请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthLogoutRequest implements Serializable {

    @Schema(name = "token", description = "令牌")
    @JsonProperty(value = "token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;
}
