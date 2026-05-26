package com.thundax.kuzhambu.interfaces.admin.audit.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "AuditOptionResponse", description = "审计选项响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditOptionResponse implements Serializable {

    @Schema(name = "value", description = "值")
    private String value;

    @Schema(name = "label", description = "标签")
    private String label;
}
