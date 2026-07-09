package com.thundax.kuzhambu.ai.infra.capability.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapability;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityMappingDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.mapper.AiCapabilityMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiCapabilityRepositoryImpl implements AiCapabilityRepository {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final AiCapabilityMapper aiCapabilityMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiCapabilityRepositoryImpl(AiCapabilityMapper aiCapabilityMapper) {
        this.aiCapabilityMapper = aiCapabilityMapper;
    }

    @Override
    public AiCapability getCapability(String capability) {
        return toCapabilityDomain(aiCapabilityMapper.selectOne(
                new LambdaQueryWrapper<AiCapabilityDO>().eq(AiCapabilityDO::getCapability, capability)));
    }

    @Override
    public List<AiCapability> listCapabilities(Boolean enabled) {
        return toCapabilityDomainList(aiCapabilityMapper.selectList(new LambdaQueryWrapper<AiCapabilityDO>()
                .eq(enabled != null, AiCapabilityDO::getEnabled, enabled)
                .orderByAsc(AiCapabilityDO::getPriority)));
    }

    @Override
    public AiCapabilityMapping getMapping(String scope, String capability) {
        return toMappingDomain(aiCapabilityMapper.selectMapping(scope, capability));
    }

    @Override
    public List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled) {
        return toMappingDomainList(aiCapabilityMapper.selectMappings(scope, capability, enabled));
    }

    @Override
    public List<AiCapabilityMapping> listMappingsByModelId(Long modelId) {
        return toMappingDomainList(aiCapabilityMapper.selectMappingsByModelId(modelId));
    }

    @Override
    public Long saveMapping(AiCapabilityMapping mapping) {
        AiCapabilityMappingDO dataObject = toMappingObject(mapping);
        if (dataObject.getMappingId() == null) {
            dataObject.setMappingId(nextId());
        }
        if (dataObject.getConfiguredAt() == null) {
            dataObject.setConfiguredAt(Instant.now());
        }
        aiCapabilityMapper.insertMapping(dataObject);
        return dataObject.getMappingId();
    }

    @Override
    public int updateMapping(AiCapabilityMapping mapping) {
        return aiCapabilityMapper.updateMapping(toMappingObject(mapping));
    }

    @Override
    public AiActionStatus getActionStatus(String scope, String capability) {
        return toActionStatusDomain(aiCapabilityMapper.selectActionStatus(scope, capability));
    }

    @Override
    public List<AiActionStatus> listActionStatuses(String scope, String capability, Boolean available) {
        return toActionStatusDomainList(aiCapabilityMapper.selectActionStatuses(scope, capability, available));
    }

    @Override
    public Long saveActionStatus(AiActionStatus actionStatus) {
        AiActionStatusDO dataObject = toActionStatusObject(actionStatus);
        if (dataObject.getActionStatusId() == null) {
            dataObject.setActionStatusId(nextId());
        }
        if (dataObject.getCheckedAt() == null) {
            dataObject.setCheckedAt(Instant.now());
        }
        aiCapabilityMapper.insertActionStatus(dataObject);
        return dataObject.getActionStatusId();
    }

    @Override
    public int updateActionStatus(AiActionStatus actionStatus) {
        return aiCapabilityMapper.updateActionStatus(toActionStatusObject(actionStatus));
    }

    private AiCapability toCapabilityDomain(AiCapabilityDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiCapability(
                dataObject.getId(),
                dataObject.getCapability(),
                dataObject.getName(),
                toStringList(dataObject.getRequiredTagsJson()),
                dataObject.getOutputMode(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    private List<AiCapability> toCapabilityDomainList(List<AiCapabilityDO> dataObjects) {
        List<AiCapability> capabilities = new ArrayList<>();
        if (dataObjects == null) {
            return capabilities;
        }
        for (AiCapabilityDO dataObject : dataObjects) {
            capabilities.add(toCapabilityDomain(dataObject));
        }
        return capabilities;
    }

    private AiCapabilityMappingDO toMappingObject(AiCapabilityMapping mapping) {
        if (mapping == null) {
            return null;
        }
        AiCapabilityMappingDO dataObject = new AiCapabilityMappingDO();
        dataObject.setId(mapping.getId());
        dataObject.setMappingId(mapping.getMappingId());
        dataObject.setScope(mapping.getScope());
        dataObject.setCapability(mapping.getCapability());
        dataObject.setModelId(mapping.getModelId());
        dataObject.setEnabled(mapping.isEnabled());
        dataObject.setConfiguredAt(mapping.getConfiguredAt());
        return dataObject;
    }

    private AiCapabilityMapping toMappingDomain(AiCapabilityMappingDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiCapabilityMapping(
                dataObject.getId(),
                dataObject.getMappingId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                dataObject.getModelId(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getConfiguredAt());
    }

    private List<AiCapabilityMapping> toMappingDomainList(List<AiCapabilityMappingDO> dataObjects) {
        List<AiCapabilityMapping> mappings = new ArrayList<>();
        if (dataObjects == null) {
            return mappings;
        }
        for (AiCapabilityMappingDO dataObject : dataObjects) {
            mappings.add(toMappingDomain(dataObject));
        }
        return mappings;
    }

    private AiActionStatusDO toActionStatusObject(AiActionStatus actionStatus) {
        if (actionStatus == null) {
            return null;
        }
        AiActionStatusDO dataObject = new AiActionStatusDO();
        dataObject.setId(actionStatus.getId());
        dataObject.setActionStatusId(actionStatus.getActionStatusId());
        dataObject.setScope(actionStatus.getScope());
        dataObject.setCapability(actionStatus.getCapability());
        dataObject.setAvailable(actionStatus.isAvailable());
        dataObject.setUnavailableReason(actionStatus.getUnavailableReason());
        dataObject.setCheckedAt(actionStatus.getCheckedAt());
        return dataObject;
    }

    private AiActionStatus toActionStatusDomain(AiActionStatusDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiActionStatus(
                dataObject.getId(),
                dataObject.getActionStatusId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                Boolean.TRUE.equals(dataObject.getAvailable()),
                dataObject.getUnavailableReason(),
                dataObject.getCheckedAt());
    }

    private List<AiActionStatus> toActionStatusDomainList(List<AiActionStatusDO> dataObjects) {
        List<AiActionStatus> statuses = new ArrayList<>();
        if (dataObjects == null) {
            return statuses;
        }
        for (AiActionStatusDO dataObject : dataObjects) {
            statuses.add(toActionStatusDomain(dataObject));
        }
        return statuses;
    }

    private List<String> toStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI capability tags can not be parsed", exception);
        }
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
