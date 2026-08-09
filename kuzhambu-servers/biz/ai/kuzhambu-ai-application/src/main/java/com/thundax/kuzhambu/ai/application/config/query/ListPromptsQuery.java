package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;

public record ListPromptsQuery(AiBusinessCapability capability, Boolean enabled) {}
