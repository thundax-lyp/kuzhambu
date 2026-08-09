package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.List;

public record PromptTemplateSaveCommand(
        PromptTemplateId id,
        AiBusinessCapability capability,
        String name,
        String description,
        boolean enabled,
        String messageTemplatesJson,
        String variablesSnapshotJson,
        String outputSchemaJson,
        String changeSummary,
        List<PromptTemplateVariableItem> variables) {}
