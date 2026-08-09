package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public record CanDispatchNextAiBatchUnitQuery(AiBatchJobId batchId) {}
