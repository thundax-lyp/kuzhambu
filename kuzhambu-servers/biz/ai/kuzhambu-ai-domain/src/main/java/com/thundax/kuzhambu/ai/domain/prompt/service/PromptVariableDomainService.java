package com.thundax.kuzhambu.ai.domain.prompt.service;

import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PromptVariableDomainService {

    public List<String> findMissingRequiredVariables(
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
}
