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
@Schema(name = "RoleUserTreeNodeResponse", description = "角色用户树节点响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleUserTreeNodeResponse implements Serializable {

    @Schema(name = "id", description = "节点ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "user", description = "用户，为空则是部门")
    @JsonProperty(value = "user")
    private RoleUserResponse user;
}
