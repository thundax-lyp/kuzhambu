package com.thundax.kuzhambu.system.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "MenuSaveRequest", description = "菜单保存请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuSaveRequest implements Serializable {

    @Schema(name = "id", description = "菜单ID")
    @JsonProperty(value = "id")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "remarks", description = "备注")
    @JsonProperty(value = "remarks")
    @Size(max = 200, message = "\"备注\"长度不能超过 200")
    private String remarks;

    @Schema(name = "parentId", description = "父节点ID")
    @JsonProperty(value = "parentId")
    @Size(max = 64, message = "父节点ID长度必须小于64")
    private String parentId;

    @Schema(name = "name", description = "名称")
    @JsonProperty(value = "name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    private String name;

    @Schema(name = "perms", description = "权限")
    @JsonProperty(value = "perms")
    private String perms;

    @Schema(name = "ranks", description = "等级", example = "0")
    @JsonProperty(value = "ranks")
    @Max(value = 9, message = "\"等级\"不能大于 9")
    private Integer ranks;

    @Schema(name = "display", description = "显示/隐藏")
    @JsonProperty(value = "display")
    private Boolean display;

    @Schema(name = "displayParams", description = "显示参数，前端使用")
    @JsonProperty(value = "displayParams")
    @Size(max = 1000, message = "\"显示参数\"长度不能超过 1000")
    private String displayParams;

    @Schema(name = "url", description = "URL")
    @JsonProperty(value = "url")
    @Size(max = 200, message = "\"URL\"长度不能超过 200")
    private String url;
}
