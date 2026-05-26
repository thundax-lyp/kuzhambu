package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditFieldResponse", description = "审计字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditFieldResponse implements Serializable {

    @Schema(name = "fieldName", description = "字段名")
    private String fieldName;

    @Schema(name = "fieldLabel", description = "字段标签")
    private String fieldLabel;

    @Schema(name = "beforeDisplayValue", description = "变更前展示值")
    private String beforeDisplayValue;

    @Schema(name = "afterDisplayValue", description = "变更后展示值")
    private String afterDisplayValue;
}
