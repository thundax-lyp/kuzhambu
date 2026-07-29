package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuildPromptOptimizationSuggestionCommand {

    private final PromptTemplateId templateId;
    private final String changeSummary;
}
