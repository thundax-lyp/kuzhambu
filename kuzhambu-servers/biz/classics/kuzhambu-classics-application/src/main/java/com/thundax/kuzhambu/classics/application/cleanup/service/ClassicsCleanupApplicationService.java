package com.thundax.kuzhambu.classics.application.cleanup.service;

import com.thundax.kuzhambu.classics.application.cleanup.result.CleanupExecutionResult;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public interface ClassicsCleanupApplicationService {

    List<CleanupTarget> listTargets(String cleanupType, Date requestedAt, Integer retentionDays, Integer limit);

    CleanupExecutionResult executeTarget(String cleanupType, Long targetId);

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class CleanupTarget {
        private final String targetType;
        private final Long targetId;
    }
}
