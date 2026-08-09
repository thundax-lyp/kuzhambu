package com.thundax.kuzhambu.ai.application.invocation.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.enums.AiBatchJobStatus;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import java.util.List;

public record AiBatchJobsByCapabilitiesQuery(
        String scope, List<AiBusinessCapability> capabilities, AiBatchJobStatus status, AiContentRef contentRef) {}
