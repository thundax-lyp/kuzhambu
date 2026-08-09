package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public record RecordAiBatchJobFailureCommand(AiBatchJobId batchId, String failureSummaryJson) {}
