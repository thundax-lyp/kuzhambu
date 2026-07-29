package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.Collection;

public class ValidatePromptVariablesCommand {

    private final PromptTemplateId templateId;
    private final Collection<String> providedNames;

    public ValidatePromptVariablesCommand(PromptTemplateId templateId, Collection<String> providedNames) {
        this.templateId = templateId;
        this.providedNames = providedNames;
    }

    public PromptTemplateId getTemplateId() {
        return templateId;
    }

    public Collection<String> getProvidedNames() {
        return providedNames;
    }
}
