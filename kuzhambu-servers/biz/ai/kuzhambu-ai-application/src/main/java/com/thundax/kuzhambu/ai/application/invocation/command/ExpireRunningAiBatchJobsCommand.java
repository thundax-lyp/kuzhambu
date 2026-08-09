package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import java.util.List;

public record ExpireRunningAiBatchJobsCommand(
        String scope,
        List<AiBusinessCapability> capabilities,
        Instant requestedBefore,
        String failureSummaryJson,
        int limit) {}
