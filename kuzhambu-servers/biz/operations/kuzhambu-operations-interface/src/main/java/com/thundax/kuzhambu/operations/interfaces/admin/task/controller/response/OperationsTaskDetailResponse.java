package com.thundax.kuzhambu.operations.interfaces.admin.task.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "OperationsTaskDetailResponse", description = "Operations 任务明细响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsTaskDetailResponse {
    private Long snapshotId;
    private String sourceDomain;
    private String taskType;
    private String taskKey;
    private String taskStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requestedByUserId;
    private Instant startedAt;
    private Instant completedAt;
    private Instant snapshotAt;
}
