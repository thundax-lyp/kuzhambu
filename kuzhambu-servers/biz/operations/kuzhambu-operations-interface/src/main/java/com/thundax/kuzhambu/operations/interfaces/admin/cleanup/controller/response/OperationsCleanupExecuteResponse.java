package com.thundax.kuzhambu.operations.interfaces.admin.cleanup.controller.response;

import java.util.Date;
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
public class OperationsCleanupExecuteResponse {
    private Long cleanupId;
    private String cleanupType;
    private String cleanupStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
}
