package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperationsCleanupExecuteResponse", description = "Operations 清理执行响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationsCleanupExecuteResponse {
    private Long cleanupId;
    private String cleanupType;
    private String cleanupStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requesterUserId;
    private Instant startedAt;
    private Instant completedAt;
}
