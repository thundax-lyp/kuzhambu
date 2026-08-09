package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;

public record AiBatchJobCreateCommand(
        String scope,
        AiBusinessCapability capability,
        AiContentRef contentRef,
        int totalCount,
        String failureSummaryJson) {}
