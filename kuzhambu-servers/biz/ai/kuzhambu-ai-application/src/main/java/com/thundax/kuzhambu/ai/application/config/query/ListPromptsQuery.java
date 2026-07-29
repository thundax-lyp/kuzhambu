package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;

public class ListPromptsQuery {

    private final AiBusinessCapability capability;
    private final Boolean enabled;

    public ListPromptsQuery(AiBusinessCapability capability, Boolean enabled) {
        this.capability = capability;
        this.enabled = enabled;
    }

    public AiBusinessCapability getCapability() {
        return capability;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
