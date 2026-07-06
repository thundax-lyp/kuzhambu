package com.thundax.kuzhambu.classics.facade.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsCleanupTargetsFacadeResponse {

    private final String cleanupType;
    private final boolean supported;
    private final String failureReason;
    private final List<Target> targets;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Target {
        private final String targetType;
        private final Long targetId;
    }
}
