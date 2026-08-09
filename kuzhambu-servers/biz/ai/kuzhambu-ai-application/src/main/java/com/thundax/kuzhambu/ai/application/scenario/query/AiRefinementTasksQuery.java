package com.thundax.kuzhambu.ai.application.scenario.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;

public record AiRefinementTasksQuery(
        AiBusinessCapability capability, AiBatchJobStatus status, AiContentRef contentRef) {}
