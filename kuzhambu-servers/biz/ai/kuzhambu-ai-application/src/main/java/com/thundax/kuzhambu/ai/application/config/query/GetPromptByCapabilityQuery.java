package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPromptByCapabilityQuery {

    private final AiBusinessCapability capability;
}
