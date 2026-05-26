package com.thundax.kuzhambu.interfaces.admin.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "DepartmentMoveRequest", description = "部门树节点移动请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentMoveRequest implements Serializable {

    public static final String TYPE_BEFORE = "before";
    public static final String TYPE_AFTER = "after";
    public static final String TYPE_INSIDE = "inside";
    public static final String TYPE_INSIDE_LAST = "insideLast";

    @Schema(name = "fromNodeId", description = "源节点")
    @JsonProperty(value = "fromNodeId")
    @NotEmpty(message = "\"源节点\"不能为空")
    private String fromNodeId;

    @Schema(name = "toNodeId", description = "目标节点")
    @JsonProperty(value = "toNodeId")
    @NotEmpty(message = "\"目标节点\"不能为空")
    private String toNodeId;

    @Schema(name = "type", description = "操作", example = TYPE_AFTER)
    @JsonProperty(value = "type")
    private String type = TYPE_AFTER;
}
