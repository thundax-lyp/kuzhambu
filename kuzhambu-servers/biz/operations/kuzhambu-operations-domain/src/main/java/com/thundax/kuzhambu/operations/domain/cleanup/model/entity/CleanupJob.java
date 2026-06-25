package com.thundax.kuzhambu.operations.domain.cleanup.model.entity;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CleanupJob {

    private CleanupJobId id;
    private String cleanupType;
    private String cleanupStatus;
    private Integer totalCount;
    private Integer successCount;
    private Integer failedCount;
    private String failureReason;
    private Long requesterUserId;
    private Date startedAt;
    private Date completedAt;
    private List<CleanupItem> cleanupItems = new ArrayList<>();
}
