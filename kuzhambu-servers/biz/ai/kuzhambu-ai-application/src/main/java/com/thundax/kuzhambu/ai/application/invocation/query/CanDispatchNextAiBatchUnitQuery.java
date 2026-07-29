package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CanDispatchNextAiBatchUnitQuery {

    private final AiBatchJobId batchId;
}
