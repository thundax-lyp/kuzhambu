package com.thundax.kuzhambu.interfaces.admin.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "RoleMenuResponse", description = "角色菜单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMenuResponse implements Serializable {

    @Schema(name = "id", description = "菜单ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "perms", description = "权限")
    @JsonProperty(value = "perms")
    private String perms;
}
