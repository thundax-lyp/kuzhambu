package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public class CanDispatchNextAiBatchUnitQuery {

    private final AiBatchJobId batchId;

    public CanDispatchNextAiBatchUnitQuery(AiBatchJobId batchId) {
        this.batchId = batchId;
    }

    public AiBatchJobId getBatchId() {
        return batchId;
    }
}
