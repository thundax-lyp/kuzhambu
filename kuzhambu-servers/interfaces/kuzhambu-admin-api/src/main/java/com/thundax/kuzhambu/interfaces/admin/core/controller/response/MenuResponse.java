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
@Schema(name = "MenuResponse", description = "菜单响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuResponse implements Serializable {

    @Schema(name = "id", description = "菜单ID")
    @JsonProperty(value = "id")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    private String remarks;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    private String name;

    @Schema(name = "perms", description = "权限")
    @JsonProperty(value = "perms")
    private String perms;

    @Schema(name = "ranks", description = "等级")
    @JsonProperty(value = "ranks")
    private Integer ranks;

    @Schema(name = "display", description = "显示/隐藏")
    @JsonProperty(value = "display")
    private Boolean display;

    @Schema(name = "displayParams", description = "显示参数，前端使用")
    @JsonProperty(value = "displayParams")
    private String displayParams;

    @Schema(name = "url", description = "URL")
    @JsonProperty(value = "url")
    private String url;
}
