package com.thundax.kuzhambu.ai.application.config.prompt.service;

import com.thundax.kuzhambu.ai.application.config.prompt.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.prompt.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.prompt.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import java.util.Collection;
import java.util.List;

public interface PromptApplicationService {

    PromptTemplate getTemplate(Long templateId);

    PromptTemplate getTemplate(String capability);

    Long saveTemplate(PromptTemplateSaveCommand command);

    PromptVersionResult getCurrentVersion(Long templateId);

    List<PromptVersionResult> listVersions(Long templateId);

    List<PromptVersionResult> compareVersions(PromptVersionCompareQuery query);

    PromptVersionResult rollback(Long templateId, int versionNo);

    List<PromptVariable> listVariables(Long templateId);

    void validateRequiredVariables(Long templateId, Collection<String> providedNames);

    PromptVersionResult buildOptimizationSuggestion(Long templateId, String changeSummary);
}
