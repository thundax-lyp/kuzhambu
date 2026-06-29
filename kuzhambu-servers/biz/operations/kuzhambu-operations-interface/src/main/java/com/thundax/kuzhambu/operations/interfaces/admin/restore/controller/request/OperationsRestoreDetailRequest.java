package com.thundax.kuzhambu.operations.interfaces.admin.restore.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsRestoreDetailRequest", description = "Operations 恢复详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsRestoreDetailRequest {

    @NotNull
    @Schema(name = "restoreId", description = "恢复记录 ID")
    @JsonProperty(value = "restoreId")
    private Long restoreId;
}
