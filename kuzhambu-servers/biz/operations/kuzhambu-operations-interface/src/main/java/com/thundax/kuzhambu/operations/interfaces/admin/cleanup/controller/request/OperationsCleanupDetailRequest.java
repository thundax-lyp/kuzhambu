package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsCleanupDetailRequest", description = "Operations 清理详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsCleanupDetailRequest {

    @NotNull
    @Schema(name = "cleanupId", description = "清理任务 ID")
    @JsonProperty(value = "cleanupId")
    private Long cleanupId;
}
