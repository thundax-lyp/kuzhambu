package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RoleAssignUserRequest", description = "角色授权用户请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAssignUserRequest implements Serializable {

    @Schema(name = "roleId", description = "角色ID")
    @JsonProperty(value = "roleId")
    @Size(max = 64, message = "\"角色ID\"长度不能超过64")
    @NotEmpty(message = "\"角色ID\"不能为空")
    private String roleId;

    @Schema(name = "users", description = "用户列表")
    @JsonProperty(value = "users")
    private List<RoleUserRequest> users;
}
