package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.time.Instant;
import java.util.List;

public class ExpireRunningAiBatchJobsCommand {

    private final String scope;
    private final List<AiBusinessCapability> capabilities;
    private final Instant requestedBefore;
    private final String failureSummaryJson;
    private final int limit;

    public ExpireRunningAiBatchJobsCommand(
            String scope,
            List<AiBusinessCapability> capabilities,
            Instant requestedBefore,
            String failureSummaryJson,
            int limit) {
        this.scope = scope;
        this.capabilities = capabilities;
        this.requestedBefore = requestedBefore;
        this.failureSummaryJson = failureSummaryJson;
        this.limit = limit;
    }

    public String getScope() {
        return scope;
    }

    public List<AiBusinessCapability> getCapabilities() {
        return capabilities;
    }

    public Instant getRequestedBefore() {
        return requestedBefore;
    }

    public String getFailureSummaryJson() {
        return failureSummaryJson;
    }

    public int getLimit() {
        return limit;
    }
}
