package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptVariable implements Sortable {

    private PromptVariableId id;
    private PromptTemplateId templateId;
    private String variableName;
    private boolean required = true;
    private String description;
    private int priority;

    public boolean isMissingIn(Iterable<String> providedNames) {
        if (!required) {
            return false;
        }
        if (providedNames == null) {
            return true;
        }
        for (String providedName : providedNames) {
            if (variableName != null && variableName.equals(providedName)) {
                return false;
            }
        }
        return true;
    }
}
