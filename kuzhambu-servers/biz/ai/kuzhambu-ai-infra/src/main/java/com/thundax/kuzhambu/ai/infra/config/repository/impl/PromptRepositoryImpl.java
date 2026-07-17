package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
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
    public PromptTemplate getTemplate(PromptTemplateId templateId) {
        return toTemplateDomain(promptMapper.selectById(value(templateId)));
    }

    @Override
    public PromptTemplate getTemplate(String scope, String capability) {
        return toTemplateDomain(promptMapper.selectTemplateByScope(scope, capability));
    }

    @Override
    public PromptTemplateId saveTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insert(dataObject);
        return PromptTemplateId.ofNullable(dataObject.getId());
    }

    @Override
    public int updateTemplate(PromptTemplate template) {
        PromptTemplateDO dataObject = toTemplateObject(template);
        return promptMapper.update(
                null,
                new LambdaUpdateWrapper<PromptTemplateDO>()
                        .eq(PromptTemplateDO::getId, dataObject.getId())
                        .set(PromptTemplateDO::getScope, dataObject.getScope())
                        .set(PromptTemplateDO::getCapability, dataObject.getCapability())
                        .set(PromptTemplateDO::getName, dataObject.getName())
                        .set(PromptTemplateDO::getDescription, dataObject.getDescription())
                        .set(PromptTemplateDO::getStatus, dataObject.getStatus()));
    }

    @Override
    public PromptVersion getCurrentVersion(PromptTemplateId templateId) {
        return toVersionDomain(promptMapper.selectCurrentVersion(value(templateId)));
    }

    @Override
    public List<PromptVersion> listVersions(PromptTemplateId templateId) {
        return toVersionDomainList(promptMapper.selectVersions(value(templateId)));
    }

    @Override
    public PromptVersionId saveVersion(PromptVersion version) {
        PromptVersionDO dataObject = toVersionObject(version);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        promptMapper.insertVersion(dataObject);
        return PromptVersionId.ofNullable(dataObject.getId());
    }

    @Override
    public int markCurrentVersion(PromptTemplateId templateId, int versionNo) {
        return promptMapper.markCurrentVersion(value(templateId), versionNo);
    }

    @Override
    public List<PromptVariable> listVariables(PromptTemplateId templateId) {
        return toVariableDomainList(promptMapper.selectVariables(value(templateId)));
    }

    @Override
    public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
        promptMapper.deleteVariables(value(templateId));
        int affectedRows = 0;
        if (variables == null) {
            return affectedRows;
        }
        for (PromptVariable variable : variables) {
            PromptVariableDO dataObject = toVariableObject(variable);
            dataObject.setTemplateId(value(templateId));
            affectedRows += promptMapper.insertVariable(dataObject);
        }
        return affectedRows;
    }

    private PromptTemplateDO toTemplateObject(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        PromptTemplateDO dataObject = new PromptTemplateDO();
        dataObject.setId(value(template.getId()));
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
                PromptTemplateId.ofNullable(dataObject.getId()),
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
        dataObject.setId(value(version.getId()));
        dataObject.setTemplateId(value(version.getTemplateId()));
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
                PromptVersionId.ofNullable(dataObject.getId()),
                PromptTemplateId.ofNullable(dataObject.getTemplateId()),
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
        dataObject.setId(value(variable.getId()));
        dataObject.setTemplateId(value(variable.getTemplateId()));
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
                PromptVariableId.ofNullable(dataObject.getId()),
                PromptTemplateId.ofNullable(dataObject.getTemplateId()),
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

    private Long value(PromptTemplateId id) {
        return id == null ? null : id.value();
    }

    private Long value(PromptVersionId id) {
        return id == null ? null : id.value();
    }

    private Long value(PromptVariableId id) {
        return id == null ? null : id.value();
    }
}
