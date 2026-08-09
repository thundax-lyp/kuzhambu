package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;

public record ListAiModelsQuery(AiApiSource apiSource, Boolean enabled) {}
