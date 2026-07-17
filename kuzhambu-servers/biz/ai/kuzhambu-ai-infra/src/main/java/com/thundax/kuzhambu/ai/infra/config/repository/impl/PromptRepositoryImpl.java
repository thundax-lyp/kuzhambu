package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVariableIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.PromptTemplateStatus;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.PromptMapper;
import java.time.Instant;
import java.util.ArrayList;
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
        return toTemplateDomain(promptMapper.selectById(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptTemplate get(String capability) {
        return toTemplateDomain(promptMapper.selectTemplateByCapability(capability));
    }

    @Override
    public PromptTemplateId insertTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insert(dataObject);
        return PromptTemplateIdCodec.toDomain(dataObject.getId());
    }

    @Override
    public int updateTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
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
        return toVersionDomain(promptMapper.selectCurrentVersion(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public List<PromptVersion> listVersions(PromptTemplateId templateId) {
        return toVersionDomainList(promptMapper.selectVersions(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public PromptVersionId insertVersion(PromptVersion version) {
        PromptVersionDO dataObject = toVersionObject(version);
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
        return toVariableDomainList(promptMapper.selectVariables(PromptTemplateIdCodec.toValue(templateId)));
    }

    @Override
    public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
        promptMapper.deleteVariables(PromptTemplateIdCodec.toValue(templateId));
        int affectedRows = 0;
        if (variables == null) {
            return affectedRows;
        }
        for (PromptVariable variable : variables) {
            PromptVariableDO dataObject = toVariableObject(variable);
            dataObject.setTemplateId(PromptTemplateIdCodec.toValue(templateId));
            affectedRows += promptMapper.insertVariable(dataObject);
        }
        return affectedRows;
    }

    private PromptTemplateDO toTemplateObject(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        PromptTemplateDO dataObject = new PromptTemplateDO();
        dataObject.setId(PromptTemplateIdCodec.toValue(template.getId()));
        dataObject.setCapability(
                template.getCapability() == null
                        ? null
                        : template.getCapability().value());
        dataObject.setName(template.getName());
        dataObject.setDescription(template.getDescription());
        dataObject.setStatus(
                template.getStatus() == null ? null : template.getStatus().value());
        dataObject.setCurrentVersionNo(template.getCurrentVersionNo());
        dataObject.setRegisteredAt(template.getRegisteredAt());
        return dataObject;
    }

    private PromptTemplate toTemplateDomain(PromptTemplateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptTemplate(
                PromptTemplateIdCodec.toDomain(dataObject.getId()),
                AiBusinessCapability.from(dataObject.getCapability()),
                dataObject.getName(),
                dataObject.getDescription(),
                PromptTemplateStatus.fromNullable(dataObject.getStatus()),
                dataObject.getCurrentVersionNo(),
                dataObject.getRegisteredAt());
    }

    private PromptVersionDO toVersionObject(PromptVersion version) {
        if (version == null) {
            return null;
        }
        PromptVersionDO dataObject = new PromptVersionDO();
        dataObject.setId(PromptVersionIdCodec.toValue(version.getId()));
        dataObject.setTemplateId(PromptTemplateIdCodec.toValue(version.getTemplateId()));
        dataObject.setVersionNo(version.getVersionNo());
        dataObject.setMessageTemplatesJson(version.getMessageTemplatesJson());
        dataObject.setVariablesSnapshotJson(version.getVariablesSnapshotJson());
        dataObject.setOutputSchemaJson(version.getOutputSchemaJson());
        dataObject.setChangeSummary(version.getChangeSummary());
        dataObject.setRegisteredAt(version.getRegisteredAt());
        return dataObject;
    }

    private PromptVersion toVersionDomain(PromptVersionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptVersion(
                PromptVersionIdCodec.toDomain(dataObject.getId()),
                PromptTemplateIdCodec.toDomain(dataObject.getTemplateId()),
                dataObject.getVersionNo() == null ? 0 : dataObject.getVersionNo(),
                dataObject.getMessageTemplatesJson(),
                dataObject.getVariablesSnapshotJson(),
                dataObject.getOutputSchemaJson(),
                dataObject.getChangeSummary(),
                dataObject.getRegisteredAt());
    }

    private List<PromptVersion> toVersionDomainList(List<PromptVersionDO> dataObjects) {
        List<PromptVersion> versions = new ArrayList<>();
        if (dataObjects == null) {
            return versions;
        }
        for (PromptVersionDO dataObject : dataObjects) {
            versions.add(toVersionDomain(dataObject));
        }
        return versions;
    }

    private PromptVariableDO toVariableObject(PromptVariable variable) {
        if (variable == null) {
            return null;
        }
        PromptVariableDO dataObject = new PromptVariableDO();
        dataObject.setId(PromptVariableIdCodec.toValue(variable.getId()));
        dataObject.setTemplateId(PromptTemplateIdCodec.toValue(variable.getTemplateId()));
        dataObject.setVariableName(variable.getVariableName());
        dataObject.setRequired(variable.isRequired());
        dataObject.setDescription(variable.getDescription());
        dataObject.setPriority(variable.getPriority());
        return dataObject;
    }

    private PromptVariable toVariableDomain(PromptVariableDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptVariable(
                PromptVariableIdCodec.toDomain(dataObject.getId()),
                PromptTemplateIdCodec.toDomain(dataObject.getTemplateId()),
                dataObject.getVariableName(),
                Boolean.TRUE.equals(dataObject.getRequired()),
                dataObject.getDescription(),
                dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    private List<PromptVariable> toVariableDomainList(List<PromptVariableDO> dataObjects) {
        List<PromptVariable> variables = new ArrayList<>();
        if (dataObjects == null) {
            return variables;
        }
        for (PromptVariableDO dataObject : dataObjects) {
            variables.add(toVariableDomain(dataObject));
        }
        return variables;
    }
}
