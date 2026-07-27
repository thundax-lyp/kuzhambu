package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import java.util.ArrayList;
import java.util.List;

public final class AiModelPersistenceAssembler {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AiModelPersistenceAssembler() {}

    public static AiModelDO toObject(AiModel model) {
        if (model == null) {
            return null;
        }
        return new AiModelDO(
                AiModelIdCodec.toValue(model.getId()),
                model.getApiSource() == null ? null : model.getApiSource().value(),
                model.getBaseUrl(),
                model.getEncryptedApiKey(),
                AiModelNameCodec.toValue(model.getModelName()),
                model.getDisplayName(),
                toCapabilityJson(model.getCapabilities()),
                model.getDefaultParamsJson(),
                model.getDescription(),
                model.isEnabled(),
                model.getRegisteredAt());
    }

    public static AiModel toDomain(AiModelDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiModel(
                AiModelIdCodec.toDomain(dataObject.getId()),
                AiApiSource.from(dataObject.getApiSource()),
                dataObject.getBaseUrl(),
                dataObject.getEncryptedApiKey(),
                AiModelNameCodec.toDomain(dataObject.getModelName()),
                dataObject.getDisplayName(),
                toCapabilities(dataObject.getCapabilitiesJson()),
                dataObject.getDefaultParamsJson(),
                dataObject.getDescription(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getRegisteredAt());
    }

    public static List<AiModel> toDomainList(List<AiModelDO> dataObjects) {
        List<AiModel> models = new ArrayList<>();
        if (dataObjects == null) {
            return models;
        }
        for (AiModelDO dataObject : dataObjects) {
            models.add(toDomain(dataObject));
        }
        return models;
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
