package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiModel {

    private AiModelId id;
    private AiApiSource apiSource;
    private String baseUrl;
    private String encryptedApiKey;
    private AiModelName modelName;
    private String displayName;
    private List<AiModelCapability> capabilities = new ArrayList<>();
    private String defaultParamsJson;
    private String description;
    private boolean enabled = true;
    private Instant registeredAt;

    public boolean supportsAll(List<AiModelCapability> requiredCapabilities) {
        if (requiredCapabilities == null || requiredCapabilities.isEmpty()) {
            return true;
        }
        return capabilities != null && capabilities.containsAll(requiredCapabilities);
    }

    public boolean canBeMappedTo(List<AiModelCapability> requiredCapabilities) {
        return enabled && supportsAll(requiredCapabilities);
    }
}
