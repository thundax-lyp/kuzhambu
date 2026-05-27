package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.thundax.kuzhambu.common.web.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuditObjectPageRequest", description = "对象审计分页请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditObjectPageRequest extends PageRequest {

    @NotBlank
    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @NotBlank
    @Schema(name = "objectId", description = "对象ID")
    private String objectId;
}
