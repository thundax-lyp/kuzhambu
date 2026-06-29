package com.thundax.kuzhambu.operations.application.cleanup.result;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsCleanupPageResult {
    private CleanupJobId cleanupId;
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
