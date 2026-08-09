package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import java.util.List;

public record CreateAiModelCommand(
        AiModelId id,
        AiApiSource apiSource,
        String baseUrl,
        String encryptedApiKey,
        AiModelName modelName,
        String displayName,
        List<AiModelCapability> capabilities,
        String defaultParamsJson,
        String description,
        Boolean enabled) {}
