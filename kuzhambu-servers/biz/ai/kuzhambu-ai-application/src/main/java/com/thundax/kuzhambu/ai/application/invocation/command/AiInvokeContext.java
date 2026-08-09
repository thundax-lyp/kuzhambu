package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;

public record AiInvokeContext(AiBatchJobId batchId, String scope, AiBusinessCapability capability) {}
