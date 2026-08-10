package com.thundax.kuzhambu.classics.application.cleanup.service;

import com.thundax.kuzhambu.classics.application.cleanup.command.ClassicsCleanupExecuteCommand;
import com.thundax.kuzhambu.classics.application.cleanup.query.ClassicsCleanupTargetsQuery;
import com.thundax.kuzhambu.classics.application.cleanup.result.CleanupExecutionResult;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public interface ClassicsCleanupApplicationService {

    List<CleanupTarget> listTargets(ClassicsCleanupTargetsQuery query);

    CleanupExecutionResult executeTarget(ClassicsCleanupExecuteCommand command);

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class CleanupTarget {
        private final String targetType;
        private final Long targetId;
    }
}
