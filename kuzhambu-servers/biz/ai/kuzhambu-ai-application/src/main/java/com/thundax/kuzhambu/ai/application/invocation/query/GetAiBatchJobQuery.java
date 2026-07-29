package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public class GetAiBatchJobQuery {

    private final AiBatchJobId batchId;

    public GetAiBatchJobQuery(AiBatchJobId batchId) {
        this.batchId = batchId;
    }

    public AiBatchJobId getBatchId() {
        return batchId;
    }
}
