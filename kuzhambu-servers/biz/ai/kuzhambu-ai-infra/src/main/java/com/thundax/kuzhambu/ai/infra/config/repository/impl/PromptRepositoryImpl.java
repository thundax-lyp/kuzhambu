package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.AiConfigPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.PromptMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PromptRepositoryImpl implements PromptRepository {

    private final PromptMapper promptMapper;

    public PromptRepositoryImpl(PromptMapper promptMapper) {
        this.promptMapper = promptMapper;
    }

    @Override
    public PromptTemplate get(PromptTemplateId templateId) {
        return AiConfigPersistenceAssembler.toTemplateDomain(
                promptMapper.selectById(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptTemplate get(String capability) {
        return AiConfigPersistenceAssembler.toTemplateDomain(promptMapper.selectTemplateByCapability(capability));
    }

    @Override
    public PromptTemplateId insertTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = AiConfigPersistenceAssembler.toObject(template);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insert(dataObject);
        return PromptTemplateIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = AiConfigPersistenceAssembler.toObject(template);
        return promptMapper.update(
                null,
                new LambdaUpdateWrapper<PromptTemplateDO>()
                        .eq(PromptTemplateDO::getId, dataObject.getId())
                        .set(PromptTemplateDO::getCapability, dataObject.getCapability())
                        .set(PromptTemplateDO::getName, dataObject.getName())
                        .set(PromptTemplateDO::getDescription, dataObject.getDescription())
                        .set(PromptTemplateDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public PromptVersion getCurrentVersion(PromptTemplateId templateId) {
        return AiConfigPersistenceAssembler.toVersionDomain(
                promptMapper.selectCurrentVersion(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public List<PromptVersion> listVersions(PromptTemplateId templateId) {
        return AiConfigPersistenceAssembler.toVersionDomainList(
                promptMapper.selectVersions(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptVersionId insertVersion(PromptVersion version) {
        PromptVersionDO dataObject = AiConfigPersistenceAssembler.toObject(version);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insertVersion(dataObject);
        return PromptVersionIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int markCurrentVersion(PromptTemplateId templateId, int versionNo) {
        return promptMapper.markCurrentVersion(PromptTemplateIdCodec.toValue(templateId), versionNo);
    }

    @Override
    public List<PromptVariable> listVariables(PromptTemplateId templateId) {
        return AiConfigPersistenceAssembler.toVariableDomainList(
                promptMapper.selectVariables(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
        promptMapper.deleteVariables(PromptTemplateIdCodec.toValue(templateId));
        int affectedRows = 0;
        if (variables == null) {
            return affectedRows;
        }
        for (PromptVariable variable : variables) {
            PromptVariableDO dataObject = AiConfigPersistenceAssembler.toObject(variable);
            dataObject.setTemplateId(PromptTemplateIdCodec.toValue(templateId));
            affectedRows += promptMapper.insertVariable(dataObject);
        }
        return affectedRows;
    }
}
