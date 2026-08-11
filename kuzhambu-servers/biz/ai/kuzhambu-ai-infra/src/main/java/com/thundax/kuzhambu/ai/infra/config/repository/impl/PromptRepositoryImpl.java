package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.PromptTemplatePersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.PromptVariablePersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.config.persistence.assembler.PromptVersionPersistenceAssembler;
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
    public PromptTemplate getTemplateById(PromptTemplateId templateId) {
        return PromptTemplatePersistenceAssembler.toDomain(
                promptMapper.selectById(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptTemplate getTemplateByCapability(AiBusinessCapability capability) {
        return PromptTemplatePersistenceAssembler.toDomain(
                promptMapper.selectTemplateByCapability(capability == null ? null : capability.value()));
    }

    @Override
    public List<PromptTemplate> list(AiBusinessCapability capability, Boolean enabled) {
        return promptMapper
                .selectList(new LambdaQueryWrapper<PromptTemplateDO>()
                        .eq(
                                capability != null,
                                PromptTemplateDO::getCapability,
                                capability == null ? null : capability.value())
                        .eq(enabled != null, PromptTemplateDO::getEnabled, enabled))
                .stream()
                .map(PromptTemplatePersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public PromptTemplateId insertTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = PromptTemplatePersistenceAssembler.toObject(template);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insert(dataObject);
        return PromptTemplateIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = PromptTemplatePersistenceAssembler.toObject(template);
        return promptMapper.update(
                null,
                new LambdaUpdateWrapper<PromptTemplateDO>()
                        .eq(PromptTemplateDO::getId, dataObject.getId())
                        .set(PromptTemplateDO::getCapability, dataObject.getCapability())
                        .set(PromptTemplateDO::getName, dataObject.getName())
                        .set(PromptTemplateDO::getDescription, dataObject.getDescription())
                        .set(PromptTemplateDO::getEnabled, dataObject.getEnabled()));
    }

    @Override
    public PromptVersion getCurrentVersionByTemplateId(PromptTemplateId templateId) {
        return PromptVersionPersistenceAssembler.toDomain(
                promptMapper.selectCurrentVersion(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptVersion getVersionById(PromptVersionId versionId) {
        return PromptVersionPersistenceAssembler.toDomain(
                promptMapper.selectVersionById(PromptVersionIdCodec.toValue(versionId)));
    }

    @Override
    public List<PromptVersion> listVersions(PromptTemplateId templateId) {
        return PromptVersionPersistenceAssembler.toDomainList(
                promptMapper.selectVersions(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptVersionId insertVersion(PromptVersion version) {
        PromptVersionDO dataObject = PromptVersionPersistenceAssembler.toObject(version);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insertVersion(dataObject);
        return PromptVersionIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateCurrentVersion(PromptTemplateId templateId, int versionNo) {
        return promptMapper.markCurrentVersion(PromptTemplateIdCodec.toValue(templateId), versionNo);
    }

    @Override
    public List<PromptVariable> listVariables(PromptTemplateId templateId) {
        return PromptVariablePersistenceAssembler.toDomainList(
                promptMapper.selectVariables(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public int replaceTemplateVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
        promptMapper.deleteVariables(PromptTemplateIdCodec.toValue(templateId));
        int affectedRows = 0;
        if (variables == null) {
            return affectedRows;
        }
        for (PromptVariable variable : variables) {
            PromptVariableDO dataObject = PromptVariablePersistenceAssembler.toObject(variable);
            dataObject.setTemplateId(PromptTemplateIdCodec.toValue(templateId));
            affectedRows += promptMapper.insertVariable(dataObject);
        }
        return affectedRows;
    }
}
