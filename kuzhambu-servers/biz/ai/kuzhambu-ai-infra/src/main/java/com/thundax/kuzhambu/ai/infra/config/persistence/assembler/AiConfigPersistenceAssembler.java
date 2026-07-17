package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVariableIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.PromptTemplateStatus;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import java.util.ArrayList;
import java.util.List;

public final class AiConfigPersistenceAssembler {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AiConfigPersistenceAssembler() {}

    public static AiModelDO toObject(AiModel model) {
        if (model == null) {
            return null;
        }
        return new AiModelDO(
                AiModelIdCodec.toValue(model.getId()),
                model.getApiSource() == null ? null : model.getApiSource().value(),
                model.getBaseUrl(),
                model.getEncryptedApiKey(),
                model.getModelName(),
                model.getDisplayName(),
                toCapabilityJson(model.getCapabilities()),
                model.getDefaultParamsJson(),
                model.getDescription(),
                model.isEnabled(),
                model.getRegisteredAt());
    }

    public static AiModel toModelDomain(AiModelDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiModel(
                AiModelIdCodec.toDomain(dataObject.getId()),
                AiApiSource.from(dataObject.getApiSource()),
                dataObject.getBaseUrl(),
                dataObject.getEncryptedApiKey(),
                dataObject.getModelName(),
                dataObject.getDisplayName(),
                toCapabilities(dataObject.getCapabilitiesJson()),
                dataObject.getDefaultParamsJson(),
                dataObject.getDescription(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getRegisteredAt());
    }

    public static List<AiModel> toModelDomainList(List<AiModelDO> dataObjects) {
        List<AiModel> models = new ArrayList<>();
        if (dataObjects == null) {
            return models;
        }
        for (AiModelDO dataObject : dataObjects) {
            models.add(toModelDomain(dataObject));
        }
        return models;
    }

    public static PromptTemplateDO toObject(PromptTemplate template) {
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

    public static PromptTemplate toTemplateDomain(PromptTemplateDO dataObject) {
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

    public static PromptVersionDO toObject(PromptVersion version) {
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

    public static PromptVersion toVersionDomain(PromptVersionDO dataObject) {
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

    public static List<PromptVersion> toVersionDomainList(List<PromptVersionDO> dataObjects) {
        List<PromptVersion> versions = new ArrayList<>();
        if (dataObjects == null) {
            return versions;
        }
        for (PromptVersionDO dataObject : dataObjects) {
            versions.add(toVersionDomain(dataObject));
        }
        return versions;
    }

    public static PromptVariableDO toObject(PromptVariable variable) {
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

    public static PromptVariable toVariableDomain(PromptVariableDO dataObject) {
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

    public static List<PromptVariable> toVariableDomainList(List<PromptVariableDO> dataObjects) {
        List<PromptVariable> variables = new ArrayList<>();
        if (dataObjects == null) {
            return variables;
        }
        for (PromptVariableDO dataObject : dataObjects) {
            variables.add(toVariableDomain(dataObject));
        }
        return variables;
    }

    private static String toCapabilityJson(List<AiModelCapability> values) {
        List<String> names = new ArrayList<>();
        if (values != null) {
            for (AiModelCapability value : values) {
                if (value != null) {
                    names.add(value.value());
                }
            }
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(names);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI model capabilities can not be serialized", exception);
        }
    }

    private static List<AiModelCapability> toCapabilities(String json) {
        List<AiModelCapability> capabilities = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return capabilities;
        }
        try {
            for (String value : OBJECT_MAPPER.readValue(json, STRING_LIST_TYPE)) {
                AiModelCapability capability = AiModelCapability.from(value);
                if (capability != null) {
                    capabilities.add(capability);
                }
            }
            return capabilities;
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI model capabilities can not be parsed", exception);
        }
    }
}
