package com.thundax.kuzhambu.system.interfaces.admin.audit.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "AuditMetaRequest", description = "审计元数据请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditMetaRequest implements Serializable {

    @NotBlank
    @Schema(name = "objectType", description = "对象类型")
    private String objectType;

    @NotBlank
    @Schema(name = "objectId", description = "对象ID")
    private String objectId;
}
