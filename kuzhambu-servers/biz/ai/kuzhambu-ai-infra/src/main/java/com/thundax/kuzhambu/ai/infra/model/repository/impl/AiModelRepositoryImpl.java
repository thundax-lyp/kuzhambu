package com.thundax.kuzhambu.ai.infra.model.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelCheckRecordDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiModelDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.dataobject.AiServiceConfigDO;
import com.thundax.kuzhambu.ai.infra.model.persistence.mapper.AiModelMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiModelRepositoryImpl implements AiModelRepository {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final AiModelMapper aiModelMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiModelRepositoryImpl(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public AiServiceConfig getServiceConfigByServiceId(Long serviceId) {
        return toServiceConfigDomain(aiModelMapper.selectServiceConfigByServiceId(serviceId));
    }

    @Override
    public AiServiceConfig getServiceConfigByRole(String serviceRole) {
        return toServiceConfigDomain(aiModelMapper.selectServiceConfigByRole(serviceRole));
    }

    @Override
    public Long saveServiceConfig(AiServiceConfig serviceConfig) {
        AiServiceConfigDO dataObject = toServiceConfigObject(serviceConfig);
        if (dataObject.getServiceId() == null) {
            dataObject.setServiceId(nextId());
        }
        if (dataObject.getConfiguredAt() == null) {
            dataObject.setConfiguredAt(Instant.now());
        }
        AiServiceConfigDO current = aiModelMapper.selectServiceConfigByServiceId(dataObject.getServiceId());
        if (current == null) {
            aiModelMapper.insertServiceConfig(dataObject);
        } else {
            aiModelMapper.updateServiceConfig(dataObject);
        }
        return dataObject.getServiceId();
    }

    @Override
    public AiModel getModelByModelId(Long modelId) {
        return toModelDomain(
                aiModelMapper.selectOne(new LambdaQueryWrapper<AiModelDO>().eq(AiModelDO::getModelId, modelId)));
    }

    @Override
    public List<AiModel> listModels(Long serviceId, Boolean enabled) {
        return toModelDomainList(aiModelMapper.selectList(new LambdaQueryWrapper<AiModelDO>()
                .eq(serviceId != null, AiModelDO::getServiceId, serviceId)
                .eq(enabled != null, AiModelDO::getEnabled, enabled)));
    }

    @Override
    public Long saveModel(AiModel model) {
        AiModelDO dataObject = toModelObject(model);
        if (dataObject.getModelId() == null) {
            dataObject.setModelId(nextId());
        }
        if (dataObject.getRegisteredAt() == null) {
            dataObject.setRegisteredAt(Instant.now());
        }
        aiModelMapper.insert(dataObject);
        return dataObject.getModelId();
    }

    @Override
    public int updateModel(AiModel model) {
        AiModelDO dataObject = toModelObject(model);
        return aiModelMapper.update(
                null,
                new LambdaUpdateWrapper<AiModelDO>()
                        .eq(AiModelDO::getModelId, dataObject.getModelId())
                        .set(AiModelDO::getServiceId, dataObject.getServiceId())
                        .set(AiModelDO::getModelName, dataObject.getModelName())
                        .set(AiModelDO::getDisplayName, dataObject.getDisplayName())
                        .set(AiModelDO::getCapabilityTagsJson, dataObject.getCapabilityTagsJson())
                        .set(AiModelDO::getDefaultParamsJson, dataObject.getDefaultParamsJson())
                        .set(AiModelDO::getDescription, dataObject.getDescription())
                        .set(AiModelDO::getEnabled, dataObject.getEnabled()));
    }

    @Override
    public int deleteModel(Long modelId) {
        return aiModelMapper.delete(new LambdaQueryWrapper<AiModelDO>().eq(AiModelDO::getModelId, modelId));
    }

    @Override
    public Long insertCheckRecord(AiModelCheckRecord record) {
        AiModelCheckRecordDO dataObject = toCheckRecordObject(record);
        if (dataObject.getCheckId() == null) {
            dataObject.setCheckId(nextId());
        }
        if (dataObject.getCheckedAt() == null) {
            dataObject.setCheckedAt(Instant.now());
        }
        aiModelMapper.insertCheckRecord(dataObject);
        return dataObject.getCheckId();
    }

    @Override
    public List<AiModelCheckRecord> listCheckRecords(Long modelId) {
        return toCheckRecordDomainList(aiModelMapper.selectCheckRecordsByModelId(modelId));
    }

    private AiServiceConfigDO toServiceConfigObject(AiServiceConfig serviceConfig) {
        if (serviceConfig == null) {
            return null;
        }
        return new AiServiceConfigDO(
                serviceConfig.getId(),
                serviceConfig.getServiceId(),
                serviceConfig.getServiceRole(),
                serviceConfig.getApiSource(),
                serviceConfig.getBaseUrl(),
                serviceConfig.getEncryptedApiKey(),
                serviceConfig.isEnabled(),
                serviceConfig.getStatus(),
                serviceConfig.getLastCheckedAt(),
                serviceConfig.getConfiguredAt());
    }

    private AiServiceConfig toServiceConfigDomain(AiServiceConfigDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiServiceConfig(
                dataObject.getId(),
                dataObject.getServiceId(),
                dataObject.getServiceRole(),
                dataObject.getApiSource(),
                dataObject.getBaseUrl(),
                dataObject.getEncryptedApiKey(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getStatus(),
                dataObject.getLastCheckedAt(),
                dataObject.getConfiguredAt());
    }

    private AiModelDO toModelObject(AiModel model) {
        if (model == null) {
            return null;
        }
        return new AiModelDO(
                model.getId(),
                model.getModelId(),
                model.getServiceId(),
                model.getModelName(),
                model.getDisplayName(),
                toJson(model.getCapabilityTags()),
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
                dataObject.getId(),
                dataObject.getModelId(),
                dataObject.getServiceId(),
                dataObject.getModelName(),
                dataObject.getDisplayName(),
                toStringList(dataObject.getCapabilityTagsJson()),
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

    private AiModelCheckRecordDO toCheckRecordObject(AiModelCheckRecord record) {
        if (record == null) {
            return null;
        }
        return new AiModelCheckRecordDO(
                record.getId(),
                record.getCheckId(),
                record.getModelId(),
                record.getServiceId(),
                record.getModelName(),
                record.getStatus(),
                record.getLatencyMs(),
                record.getErrorType(),
                record.getErrorMessage(),
                record.getCheckedAt());
    }

    private AiModelCheckRecord toCheckRecordDomain(AiModelCheckRecordDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiModelCheckRecord(
                dataObject.getId(),
                dataObject.getCheckId(),
                dataObject.getModelId(),
                dataObject.getServiceId(),
                dataObject.getModelName(),
                dataObject.getStatus(),
                dataObject.getLatencyMs(),
                dataObject.getErrorType(),
                dataObject.getErrorMessage(),
                dataObject.getCheckedAt());
    }

    private List<AiModelCheckRecord> toCheckRecordDomainList(List<AiModelCheckRecordDO> dataObjects) {
        List<AiModelCheckRecord> records = new ArrayList<>();
        if (dataObjects == null) {
            return records;
        }
        for (AiModelCheckRecordDO dataObject : dataObjects) {
            records.add(toCheckRecordDomain(dataObject));
        }
        return records;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? new ArrayList<>() : values);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI model tags can not be serialized", exception);
        }
    }

    private List<String> toStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI model tags can not be parsed", exception);
        }
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
