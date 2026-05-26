package com.thundax.kuzhambu.interfaces.admin.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuditLogDetailRequest", description = "审计日志详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogDetailRequest implements Serializable {

    @NotNull
    @Schema(name = "id", description = "审计日志ID")
    private String id;
}
