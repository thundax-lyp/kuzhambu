package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;

public class GetPromptByCapabilityQuery {

    private final AiBusinessCapability capability;

    public GetPromptByCapabilityQuery(AiBusinessCapability capability) {
        this.capability = capability;
    }

    public AiBusinessCapability getCapability() {
        return capability;
    }
}
