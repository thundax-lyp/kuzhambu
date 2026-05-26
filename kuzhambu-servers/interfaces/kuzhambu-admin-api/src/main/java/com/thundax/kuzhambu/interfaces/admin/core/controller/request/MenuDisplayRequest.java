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
@Schema(name = "MenuDisplayRequest", description = "菜单显示状态请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuDisplayRequest implements Serializable {

    @Schema(name = "id", description = "菜单ID")
    @JsonProperty(value = "id")
    @NotEmpty(message = "ID不能为空")
    @Size(max = 64, message = "ID长度不能超过64")
    private String id;

    @Schema(name = "display", description = "显示/隐藏")
    @JsonProperty(value = "display")
    private Boolean display;
}
