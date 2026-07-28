package com.thundax.kuzhambu.classics.application.cleanup.result;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CleanupExecutionResult {
    private final String targetType;
    private final Long targetId;
    private final boolean success;
    private final String failureReason;
}
