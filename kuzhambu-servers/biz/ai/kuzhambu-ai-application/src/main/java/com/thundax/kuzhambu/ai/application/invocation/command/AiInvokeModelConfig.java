package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;

public record AiInvokeModelConfig(Long serviceId, String serviceRole, AiModelId modelId, AiModelName modelName) {}
