package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public class RecordAiBatchJobCommand {

    private final AiBatchJobId batchId;

    public RecordAiBatchJobCommand(AiBatchJobId batchId) {
        this.batchId = batchId;
    }

    public AiBatchJobId getBatchId() {
        return batchId;
    }
}
