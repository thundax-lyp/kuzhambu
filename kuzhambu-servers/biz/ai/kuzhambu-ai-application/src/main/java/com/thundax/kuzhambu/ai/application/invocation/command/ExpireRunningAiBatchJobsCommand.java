package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpireRunningAiBatchJobsCommand {

    private final String scope;
    private final List<AiBusinessCapability> capabilities;
    private final Instant requestedBefore;
    private final String failureSummaryJson;
    private final int limit;
}
