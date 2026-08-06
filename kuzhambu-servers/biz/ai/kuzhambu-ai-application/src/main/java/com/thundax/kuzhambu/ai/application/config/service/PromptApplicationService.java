package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.application.config.command.BuildPromptOptimizationSuggestionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ChangePromptTemplateStatusCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeletePromptTemplateCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.command.RollbackPromptVersionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ValidatePromptVariablesCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetCurrentPromptVersionQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVariablesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVersionsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptsQuery;
import com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import java.util.List;

public interface PromptApplicationService {

    PromptTemplate get(GetPromptQuery query);

    PromptTemplate getByCapability(GetPromptByCapabilityQuery query);

    List<PromptTemplate> list(ListPromptsQuery query);

    PromptTemplateId save(PromptTemplateSaveCommand command);

    void changeStatus(ChangePromptTemplateStatusCommand command);

    void delete(DeletePromptTemplateCommand command);

    PromptVersionResult getCurrentVersion(GetCurrentPromptVersionQuery query);

    List<PromptVersionResult> listVersions(ListPromptVersionsQuery query);

    List<PromptVersionResult> compareVersions(PromptVersionCompareQuery query);

    PromptVersionResult rollback(RollbackPromptVersionCommand command);

    List<PromptVariable> listVariables(ListPromptVariablesQuery query);

    void validateRequiredVariables(ValidatePromptVariablesCommand command);

    PromptVersionResult buildOptimizationSuggestion(BuildPromptOptimizationSuggestionCommand command);
}
