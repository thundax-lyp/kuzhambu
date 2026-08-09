package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.Collection;

public record ValidatePromptVariablesCommand(PromptTemplateId templateId, Collection<String> providedNames) {}
