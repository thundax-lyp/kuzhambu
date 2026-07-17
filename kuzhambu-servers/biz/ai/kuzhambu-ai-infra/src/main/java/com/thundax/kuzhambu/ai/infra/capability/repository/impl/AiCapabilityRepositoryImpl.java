package com.thundax.kuzhambu.ai.infra.capability.repository.impl;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityMappingDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.mapper.AiCapabilityMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AiCapabilityRepositoryImpl implements AiCapabilityRepository {

    private final AiCapabilityMapper aiCapabilityMapper;
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

    public AiCapabilityRepositoryImpl(AiCapabilityMapper aiCapabilityMapper) {
        this.aiCapabilityMapper = aiCapabilityMapper;
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

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
