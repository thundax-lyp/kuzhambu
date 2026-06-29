package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsCleanupExecuteRequest", description = "Operations 清理执行请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsCleanupExecuteRequest {

    @NotBlank
    @Schema(name = "cleanupType", description = "清理类型")
    @JsonProperty(value = "cleanupType")
    private String cleanupType;
}
