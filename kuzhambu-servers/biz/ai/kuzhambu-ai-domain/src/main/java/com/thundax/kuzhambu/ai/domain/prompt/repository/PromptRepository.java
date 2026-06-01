package com.thundax.kuzhambu.ai.domain.prompt.repository;

import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVersion;
import java.util.List;

public interface PromptRepository {

    PromptTemplate getTemplate(Long templateId);

    PromptTemplate getTemplate(String scope, String capability);

    Long saveTemplate(PromptTemplate template);

    int updateTemplate(PromptTemplate template);

    PromptVersion getCurrentVersion(Long templateId);

    List<PromptVersion> listVersions(Long templateId);

    Long saveVersion(PromptVersion version);

    int markCurrentVersion(Long templateId, int versionNo);

    List<PromptVariable> listVariables(Long templateId);

    int replaceVariables(Long templateId, List<PromptVariable> variables);
}
