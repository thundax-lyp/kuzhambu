package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

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
@Schema(name = "RoleSaveRequest", description = "角色保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleSaveRequest implements Serializable {

    @Schema(name = "id", description = "角色ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    private String name;

    @Schema(name = "admin", description = "是否管理权限")
    @JsonProperty(value = "admin")
    private Boolean admin;

    @Schema(name = "enable", description = "启用/禁用")
    @JsonProperty(value = "enable")
    private Boolean enable;

    @Schema(name = "menus", description = "菜单列表")
    @JsonProperty(value = "menus")
    private List<RoleMenuRequest> menuList;
}
