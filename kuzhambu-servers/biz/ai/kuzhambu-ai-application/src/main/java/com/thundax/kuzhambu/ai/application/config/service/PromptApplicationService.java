package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.Collection;
import java.util.List;

public interface PromptApplicationService {

    PromptTemplate getTemplate(PromptTemplateId templateId);

    PromptTemplate getTemplate(AiBusinessCapability capability);

    List<PromptTemplate> listTemplates(AiBusinessCapability capability, Boolean enabled);

    PromptTemplateId saveTemplate(PromptTemplateSaveCommand command);

    PromptVersionResult getCurrentVersion(PromptTemplateId templateId);

    List<PromptVersionResult> listVersions(PromptTemplateId templateId);

    List<PromptVersionResult> compareVersions(PromptVersionCompareQuery query);

    PromptVersionResult rollback(PromptTemplateId templateId, int versionNo);

    List<PromptVariable> listVariables(PromptTemplateId templateId);

    void validateRequiredVariables(PromptTemplateId templateId, Collection<String> providedNames);

    PromptVersionResult buildOptimizationSuggestion(PromptTemplateId templateId, String changeSummary);
}
