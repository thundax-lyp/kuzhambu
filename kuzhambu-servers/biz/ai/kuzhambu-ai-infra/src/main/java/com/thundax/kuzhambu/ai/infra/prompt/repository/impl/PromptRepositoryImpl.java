package com.thundax.kuzhambu.ai.infra.prompt.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.prompt.repository.PromptRepository;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.dataobject.PromptVersionDO;
import com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper.PromptMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PromptRepositoryImpl implements PromptRepository {

    private final PromptMapper promptMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public PromptRepositoryImpl(PromptMapper promptMapper) {
        this.promptMapper = promptMapper;
    }

    @Override
    public PromptTemplate getTemplate(Long templateId) {
        return toTemplateDomain(promptMapper.selectOne(
                new LambdaQueryWrapper<PromptTemplateDO>().eq(PromptTemplateDO::getTemplateId, templateId)));
    }

    @Override
    public PromptTemplate getTemplate(String scope, String capability) {
        return toTemplateDomain(promptMapper.selectTemplateByScope(scope, capability));
    }

    @Override
    public Long saveTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
        if (dataObject.getTemplateId() == null) {
            dataObject.setTemplateId(nextId());
        }
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insert(dataObject);
        return dataObject.getTemplateId();
    }

    @Override
    public int updateTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
        return promptMapper.update(
                null,
                new LambdaUpdateWrapper<PromptTemplateDO>()
                        .eq(PromptTemplateDO::getTemplateId, dataObject.getTemplateId())
                        .set(PromptTemplateDO::getScope, dataObject.getScope())
                        .set(PromptTemplateDO::getCapability, dataObject.getCapability())
                        .set(PromptTemplateDO::getName, dataObject.getName())
                        .set(PromptTemplateDO::getDescription, dataObject.getDescription())
                        .set(PromptTemplateDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public PromptVersion getCurrentVersion(Long templateId) {
        return toVersionDomain(promptMapper.selectCurrentVersion(templateId));
    }

    @Override
    public List<PromptVersion> listVersions(Long templateId) {
        return toVersionDomainList(promptMapper.selectVersions(templateId));
    }

    @Override
    public Long saveVersion(PromptVersion version) {
        PromptVersionDO dataObject = toVersionObject(version);
        if (dataObject.getPromptVersionId() == null) {
            dataObject.setPromptVersionId(nextId());
        }
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insertVersion(dataObject);
        return dataObject.getPromptVersionId();
    }

    @Override
    public int markCurrentVersion(Long templateId, int versionNo) {
        promptMapper.clearCurrentVersion(templateId);
        int affectedRows = promptMapper.markCurrentVersion(templateId, versionNo);
        if (affectedRows > 0) {
            promptMapper.updateTemplateCurrentVersion(templateId, versionNo);
        }
        return affectedRows;
    }

    @Override
    public List<PromptVariable> listVariables(Long templateId) {
        return toVariableDomainList(promptMapper.selectVariables(templateId));
    }

    @Override
    public int replaceVariables(Long templateId, List<PromptVariable> variables) {
        promptMapper.deleteVariables(templateId);
        int affectedRows = 0;
        if (variables == null) {
            return affectedRows;
        }
        for (PromptVariable variable : variables) {
            PromptVariableDO dataObject = toVariableObject(variable);
            dataObject.setTemplateId(templateId);
            if (dataObject.getVariableId() == null) {
                dataObject.setVariableId(nextId());
            }
            affectedRows += promptMapper.insertVariable(dataObject);
        }
        return affectedRows;
    }

    private PromptTemplateDO toTemplateObject(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        PromptTemplateDO dataObject = new PromptTemplateDO();
        dataObject.setId(template.getId());
        dataObject.setTemplateId(template.getTemplateId());
        dataObject.setScope(template.getScope());
        dataObject.setCapability(template.getCapability());
        dataObject.setName(template.getName());
        dataObject.setDescription(template.getDescription());
        dataObject.setStatus(template.getStatus());
        dataObject.setCurrentVersionNo(template.getCurrentVersionNo());
        dataObject.setRegisteredAt(template.getRegisteredAt());
        return dataObject;
    }

    private PromptTemplate toTemplateDomain(PromptTemplateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptTemplate(
                dataObject.getId(),
                dataObject.getTemplateId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getName(),
                dataObject.getDescription(),
                dataObject.getStatus(),
                dataObject.getCurrentVersionNo(),
                dataObject.getRegisteredAt());
    }

    private PromptVersionDO toVersionObject(PromptVersion version) {
        if (version == null) {
            return null;
        }
        PromptVersionDO dataObject = new PromptVersionDO();
        dataObject.setId(version.getId());
        dataObject.setPromptVersionId(version.getPromptVersionId());
        dataObject.setTemplateId(version.getTemplateId());
        dataObject.setVersionNo(version.getVersionNo());
        dataObject.setMessageTemplatesJson(version.getMessageTemplatesJson());
        dataObject.setVariablesSnapshotJson(version.getVariablesSnapshotJson());
        dataObject.setOutputSchemaJson(version.getOutputSchemaJson());
        dataObject.setCurrentKey(version.getCurrentKey());
        dataObject.setChangeSummary(version.getChangeSummary());
        dataObject.setRegisteredAt(version.getRegisteredAt());
        return dataObject;
    }

    private PromptVersion toVersionDomain(PromptVersionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptVersion(
                dataObject.getId(),
                dataObject.getPromptVersionId(),
                dataObject.getTemplateId(),
                dataObject.getVersionNo() == null ? 0 : dataObject.getVersionNo(),
                dataObject.getMessageTemplatesJson(),
                dataObject.getVariablesSnapshotJson(),
                dataObject.getOutputSchemaJson(),
                dataObject.getCurrentKey(),
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
        dataObject.setId(variable.getId());
        dataObject.setVariableId(variable.getVariableId());
        dataObject.setTemplateId(variable.getTemplateId());
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
                dataObject.getId(),
                dataObject.getVariableId(),
                dataObject.getTemplateId(),
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

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
