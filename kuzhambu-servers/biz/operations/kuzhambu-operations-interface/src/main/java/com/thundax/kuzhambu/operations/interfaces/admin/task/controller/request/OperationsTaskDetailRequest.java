package com.thundax.kuzhambu.operations.interfaces.admin.task.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "OperationsTaskDetailRequest", description = "Operations 长任务详情请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsTaskDetailRequest {

    @NotNull
    @Schema(name = "snapshotId", description = "任务快照 ID")
    @JsonProperty(value = "snapshotId")
    private Long snapshotId;
}
