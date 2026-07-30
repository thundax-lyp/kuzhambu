package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public static List<String> findMissingRequiredVariables(
            List<PromptVariable> requiredVariables, Collection<String> providedNames) {
        List<String> missingNames = new ArrayList<>();
        if (requiredVariables == null || requiredVariables.isEmpty()) {
            return missingNames;
        }
        for (PromptVariable variable : requiredVariables) {
            if (variable.isMissingIn(providedNames)) {
                missingNames.add(variable.getVariableName());
            }
        }
        return missingNames;
    }

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
