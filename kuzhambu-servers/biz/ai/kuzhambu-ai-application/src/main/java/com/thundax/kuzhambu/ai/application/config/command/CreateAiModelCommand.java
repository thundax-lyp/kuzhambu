package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAiModelCommand {

    private final AiModelId id;
    private final AiApiSource apiSource;
    private final String baseUrl;
    private final String encryptedApiKey;
    private final AiModelName modelName;
    private final String displayName;
    private final List<AiModelCapability> capabilities;
    private final String defaultParamsJson;
    private final String description;
    private final Boolean enabled;
}
