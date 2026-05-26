package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "PersonalInfoUpdateRequest", description = "当前用户资料更新请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalInfoUpdateRequest implements Serializable {

    @Schema(name = "name", description = "姓名")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"姓名\"不能为空")
    @Size(max = 50, message = "\"姓名\"长度不能超过 50")
    private String name;

    @Schema(name = "email", description = "邮箱")
    @JsonProperty(value = "email")
    @Size(max = 50, message = "\"邮箱\"长度不能超过 50")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    @JsonProperty(value = "mobile")
    @Size(max = 30, message = "\"手机号\"长度不能超过 30")
    private String mobile;
}
