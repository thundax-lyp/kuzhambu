package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

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
@Schema(name = "UserDepartmentRequest", description = "用户部门请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDepartmentRequest implements Serializable {

    @Schema(name = "id", description = "部门ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "部门ID不能为空")
    @Size(max = 64, message = "部门ID长度不能超过64")
    private String id;
}
