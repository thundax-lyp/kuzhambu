package com.thundax.kuzhambu.classics.facade.request;

import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsCleanupTargetsFacadeRequest {

    private final String cleanupType;
    private final List<Long> targetIds;
    private final Integer retentionDays;
    private final Integer limit;
    private final Date requestedAt;
}
