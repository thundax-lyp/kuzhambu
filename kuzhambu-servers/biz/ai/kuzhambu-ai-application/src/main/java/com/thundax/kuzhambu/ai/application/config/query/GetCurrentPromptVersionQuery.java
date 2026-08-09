package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public record GetCurrentPromptVersionQuery(PromptTemplateId templateId) {}
