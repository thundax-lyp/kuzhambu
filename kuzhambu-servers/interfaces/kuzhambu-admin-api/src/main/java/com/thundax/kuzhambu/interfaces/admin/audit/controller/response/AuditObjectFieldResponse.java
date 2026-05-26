package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditObjectFieldResponse", description = "审计对象字段响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectFieldResponse implements Serializable {

    @Schema(name = "fieldName", description = "字段名")
    private String fieldName;

    @Schema(name = "fieldLabel", description = "字段标签")
    private String fieldLabel;
}
