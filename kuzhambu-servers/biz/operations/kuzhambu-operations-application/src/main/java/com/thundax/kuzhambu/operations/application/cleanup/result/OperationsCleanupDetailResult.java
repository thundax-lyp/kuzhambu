package com.thundax.kuzhambu.operations.application.cleanup.result;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsCleanupDetailResult {
    private CleanupJobId cleanupId;
    private String cleanupType;
    private String cleanupStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requesterUserId;
    private Instant startedAt;
    private Instant completedAt;
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private CleanupItemId cleanupItemId;
        private String targetType;
        private Long targetId;
        private String itemStatus;
        private String failureReason;
        private Instant processedAt;
    }
}
