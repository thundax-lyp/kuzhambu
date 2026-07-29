package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordAiBatchJobFailureCommand {

    private final AiBatchJobId batchId;
    private final String failureSummaryJson;
}
