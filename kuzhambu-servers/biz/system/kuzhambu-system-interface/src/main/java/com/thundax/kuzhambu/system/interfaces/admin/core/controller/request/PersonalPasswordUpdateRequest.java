package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

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
@Schema(name = "PersonalPasswordUpdateRequest", description = "当前用户密码更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalPasswordUpdateRequest implements Serializable {

    @Schema(name = "oldPassword", description = "旧密码")
    @JsonProperty(value = "oldPassword")
    @NotEmpty(message = "\"旧密码\"不能为空")
    private String oldPassword;

    @Schema(name = "password", description = "新密码")
    @JsonProperty(value = "password")
    @NotEmpty(message = "\"新密码\"不能为空")
    private String password;

    @Schema(name = "token", description = "令牌")
    @JsonProperty(value = "token")
    @NotEmpty(message = "\"token\"不能为空")
    private String token;
}
