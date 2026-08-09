package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public record UpdateAiBusinessConfigCommand(
        AiBusinessConfigId id,
        AiBusinessCapability capability,
        PromptTemplateId promptTemplateId,
        AiModelId modelId,
        String defaultParamsJson,
        Boolean enabled) {}
