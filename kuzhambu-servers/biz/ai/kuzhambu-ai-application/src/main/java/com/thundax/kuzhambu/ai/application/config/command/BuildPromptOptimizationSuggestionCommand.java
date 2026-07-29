package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public class BuildPromptOptimizationSuggestionCommand {

    private final PromptTemplateId templateId;
    private final String changeSummary;

    public BuildPromptOptimizationSuggestionCommand(PromptTemplateId templateId, String changeSummary) {
        this.templateId = templateId;
        this.changeSummary = changeSummary;
    }

    public PromptTemplateId getTemplateId() {
        return templateId;
    }

    public String getChangeSummary() {
        return changeSummary;
    }
}
