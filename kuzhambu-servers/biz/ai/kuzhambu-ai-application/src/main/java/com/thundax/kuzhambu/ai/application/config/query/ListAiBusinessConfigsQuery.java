package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;

public record ListAiBusinessConfigsQuery(AiBusinessCapability capability, Boolean enabled) {}
