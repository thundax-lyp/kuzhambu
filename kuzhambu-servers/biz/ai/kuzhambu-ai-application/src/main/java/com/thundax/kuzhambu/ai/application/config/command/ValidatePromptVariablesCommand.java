package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidatePromptVariablesCommand {

    private final PromptTemplateId templateId;
    private final Collection<String> providedNames;
}
