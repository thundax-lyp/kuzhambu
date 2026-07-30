package com.thundax.kuzhambu.operations.domain.cleanup.model.entity;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupItemId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CleanupItem {

    private CleanupItemId id;
    private Long cleanupId;
    private String targetType;
    private Long targetId;
    private String itemStatus;
    private String failureReason;
    private Instant processedAt;
}
