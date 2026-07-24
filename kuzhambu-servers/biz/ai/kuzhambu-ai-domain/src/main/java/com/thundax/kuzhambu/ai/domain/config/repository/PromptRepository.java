package com.thundax.kuzhambu.ai.domain.config.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import java.util.List;

public interface PromptRepository {

    PromptTemplate get(PromptTemplateId templateId);

    PromptTemplate get(AiBusinessCapability capability);

    List<PromptTemplate> list(AiBusinessCapability capability, Boolean enabled);

    PromptTemplateId insertTemplate(PromptTemplate template);

    int updateTemplate(PromptTemplate template);

    PromptVersion getCurrentVersion(PromptTemplateId templateId);

    PromptVersion getVersion(PromptVersionId versionId);

    List<PromptVersion> listVersions(PromptTemplateId templateId);

    PromptVersionId insertVersion(PromptVersion version);

    int markCurrentVersion(PromptTemplateId templateId, int versionNo);

    List<PromptVariable> listVariables(PromptTemplateId templateId);

    int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables);
}
