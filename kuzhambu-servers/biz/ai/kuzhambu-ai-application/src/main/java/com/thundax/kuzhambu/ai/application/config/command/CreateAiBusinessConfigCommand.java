package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAiBusinessConfigCommand {

    private final AiBusinessConfigId id;
    private final AiBusinessCapability capability;
    private final PromptTemplateId promptTemplateId;
    private final AiModelId modelId;
    private final String defaultParamsJson;
    private final Boolean enabled;
}
