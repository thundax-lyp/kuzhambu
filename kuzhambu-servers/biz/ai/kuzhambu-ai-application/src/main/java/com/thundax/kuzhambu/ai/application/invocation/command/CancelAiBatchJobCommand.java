package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public class CancelAiBatchJobCommand {

    private final AiBatchJobId batchId;

    public CancelAiBatchJobCommand(AiBatchJobId batchId) {
        this.batchId = batchId;
    }

    public AiBatchJobId getBatchId() {
        return batchId;
    }
}
