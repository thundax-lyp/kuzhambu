package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public class RecordAiBatchJobFailureCommand {

    private final AiBatchJobId batchId;
    private final String failureSummaryJson;

    public RecordAiBatchJobFailureCommand(AiBatchJobId batchId, String failureSummaryJson) {
        this.batchId = batchId;
        this.failureSummaryJson = failureSummaryJson;
    }

    public AiBatchJobId getBatchId() {
        return batchId;
    }

    public String getFailureSummaryJson() {
        return failureSummaryJson;
    }
}
