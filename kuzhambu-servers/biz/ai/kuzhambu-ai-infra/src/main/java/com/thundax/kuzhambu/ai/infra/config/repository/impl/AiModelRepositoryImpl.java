package com.thundax.kuzhambu.ai.infra.config.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.config.persistence.mapper.AiModelMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiModelRepositoryImpl implements AiModelRepository {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final AiModelMapper aiModelMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiModelRepositoryImpl(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public AiModel getModelById(AiModelId id) {
        return toModelDomain(aiModelMapper.selectById(id == null ? null : id.value()));
    }

    @Override
    public List<AiModel> listModels(String apiSource, Boolean enabled) {
        return toModelDomainList(aiModelMapper.selectList(new LambdaQueryWrapper<AiModelDO>()
                .eq(apiSource != null && !apiSource.isBlank(), AiModelDO::getApiSource, apiSource)
                .eq(enabled != null, AiModelDO::getEnabled, enabled)));
    }

    @Override
    public AiModelId saveModel(AiModel model) {
        AiModelDO dataObject = toModelObject(model);
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        aiModelMapper.insert(dataObject);
        return AiModelId.ofNullable(dataObject.getId());
    }

    @Override
    public int updateModel(AiModel model) {
        AiModelDO dataObject = toModelObject(model);
        return aiModelMapper.update(
                null,
                new LambdaUpdateWrapper<AiModelDO>()
                        .eq(AiModelDO::getId, dataObject.getId())
                        .set(AiModelDO::getApiSource, dataObject.getApiSource())
                        .set(AiModelDO::getBaseUrl, dataObject.getBaseUrl())
                        .set(AiModelDO::getEncryptedApiKey, dataObject.getEncryptedApiKey())
                        .set(AiModelDO::getModelName, dataObject.getModelName())
                        .set(AiModelDO::getDisplayName, dataObject.getDisplayName())
                        .set(AiModelDO::getCapabilitiesJson, dataObject.getCapabilitiesJson())
                        .set(AiModelDO::getDefaultParamsJson, dataObject.getDefaultParamsJson())
                        .set(AiModelDO::getDescription, dataObject.getDescription())
                        .set(AiModelDO::getEnabled, dataObject.getEnabled()));
    }

    @Override
    public int deleteModel(AiModelId id) {
        return aiModelMapper.deleteById(id == null ? null : id.value());
    }

    private AiModelDO toModelObject(AiModel model) {
        if (model == null) {
            return null;
        }
        return new AiModelDO(
                value(model.getId()),
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

    private AiModel toModelDomain(AiModelDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiModel(
                AiModelId.ofNullable(dataObject.getId()),
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

    private List<AiModel> toModelDomainList(List<AiModelDO> dataObjects) {
        List<AiModel> models = new ArrayList<>();
        if (dataObjects == null) {
            return models;
        }
        for (AiModelDO dataObject : dataObjects) {
            models.add(toModelDomain(dataObject));
        }
        return models;
    }

    private String toCapabilityJson(List<AiModelCapability> values) {
        List<String> names = new ArrayList<>();
        if (values != null) {
            for (AiModelCapability value : values) {
                if (value != null) {
                    names.add(value.value());
                }
            }
        }
        try {
            return objectMapper.writeValueAsString(names);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI model capabilities can not be serialized", exception);
        }
    }

    private List<AiModelCapability> toCapabilities(String json) {
        List<AiModelCapability> capabilities = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return capabilities;
        }
        try {
            for (String value : objectMapper.readValue(json, STRING_LIST_TYPE)) {
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

    private Long value(AiModelId id) {
        return id == null ? null : id.value();
    }
}
