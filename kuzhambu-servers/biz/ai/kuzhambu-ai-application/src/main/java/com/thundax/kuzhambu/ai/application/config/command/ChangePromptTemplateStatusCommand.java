package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public record ChangePromptTemplateStatusCommand(PromptTemplateId templateId, boolean enabled) {}
