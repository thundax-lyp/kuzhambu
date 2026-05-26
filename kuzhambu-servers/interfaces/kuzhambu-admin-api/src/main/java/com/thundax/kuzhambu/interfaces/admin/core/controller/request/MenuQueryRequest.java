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
@Schema(name = "MenuQueryRequest", description = "菜单查询请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuQueryRequest implements Serializable {

    @Schema(name = "parentId", description = "父节点ID，为\"ROOT\"则查询跟节点")
    @JsonProperty(value = "parentId")
    @Size(max = 64, message = "\"父节点ID\"长度不能超过 64")
    private String parentId;

    @Schema(name = "display", description = "显示/隐藏")
    @JsonProperty(value = "display")
    private Boolean display;
}
