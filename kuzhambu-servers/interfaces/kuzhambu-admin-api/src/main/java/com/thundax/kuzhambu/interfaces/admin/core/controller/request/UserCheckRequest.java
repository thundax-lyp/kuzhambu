package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UserCheckRequest", description = "用户唯一性检查请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCheckRequest implements Serializable {

    @Schema(name = "id", description = "用户ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "loginName", description = "登录名")
    @JsonProperty(value = "loginName")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    private String loginName;
}
