package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditSnapshotFieldResponse", description = "审计快照字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditSnapshotFieldResponse implements Serializable {

    @Schema(name = "fieldName", description = "字段名")
    private String fieldName;

    @Schema(name = "fieldLabel", description = "字段标签")
    private String fieldLabel;

    @Schema(name = "value", description = "原始值")
    private Object value;

    @Schema(name = "displayValue", description = "展示值")
    private String displayValue;

    @Schema(name = "valueType", description = "值类型")
    private String valueType;

    @Schema(name = "sensitive", description = "是否敏感")
    private Boolean sensitive;
}
