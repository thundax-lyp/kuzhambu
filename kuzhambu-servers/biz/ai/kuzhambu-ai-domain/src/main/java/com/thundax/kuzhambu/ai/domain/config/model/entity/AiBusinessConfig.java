package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiBusinessConfig implements Sortable {

    private AiBusinessConfigId id;
    private AiBusinessCapability capability;
    private PromptTemplateId promptTemplateId;
    private AiModelId modelId;
    private String defaultParamsJson;
    private boolean enabled = true;
    private int priority;
    private Instant configuredAt;

    public boolean canUse(PromptTemplate promptTemplate, AiModel model) {
        return enabled && promptMatches(promptTemplate) && modelMatches(model);
    }

    public boolean promptMatches(PromptTemplate promptTemplate) {
        return promptTemplate != null
                && promptTemplate.isEnabled()
                && capability != null
                && promptTemplate.getCapability() == capability
                && promptTemplateId != null
                && promptTemplateId.equals(promptTemplate.getId());
    }

    public boolean modelMatches(AiModel model) {
        return model != null
                && modelId != null
                && modelId.equals(model.getId())
                && model.canBeMappedTo(capability == null ? null : capability.requiredModelCapabilities());
    }
}
