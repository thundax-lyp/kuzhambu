package com.thundax.kuzhambu.system.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "RoleResponse", description = "角色响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleResponse implements Serializable {

    @Schema(name = "id", description = "角色ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "admin", description = "是否管理权限")
    @JsonProperty(value = "admin")
    private Boolean admin;

    @Schema(name = "enable", description = "启用/禁用")
    @JsonProperty(value = "enable")
    private Boolean enable;

    @Schema(name = "menus", description = "菜单列表")
    @JsonProperty(value = "menus")
    private List<RoleMenuResponse> menuList;
}
