package com.thundax.kuzhambu.ai.domain.config.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import java.util.List;

public interface PromptRepository {

    PromptTemplate getTemplate(PromptTemplateId templateId);

    PromptTemplate getTemplate(String scope, String capability);

    PromptTemplateId saveTemplate(PromptTemplate template);

    int updateTemplate(PromptTemplate template);

    PromptVersion getCurrentVersion(PromptTemplateId templateId);

    List<PromptVersion> listVersions(PromptTemplateId templateId);

    PromptVersionId saveVersion(PromptVersion version);

    int markCurrentVersion(PromptTemplateId templateId, int versionNo);

    List<PromptVariable> listVariables(PromptTemplateId templateId);

    int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables);
}
