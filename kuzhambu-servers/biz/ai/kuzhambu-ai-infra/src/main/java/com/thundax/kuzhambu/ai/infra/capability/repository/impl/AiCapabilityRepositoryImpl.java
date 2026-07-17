package com.thundax.kuzhambu.ai.infra.capability.repository.impl;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.infra.capability.persistence.assembler.AiActionStatusPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.capability.persistence.assembler.AiCapabilityMappingPersistenceAssembler;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityMappingDO;
import com.thundax.kuzhambu.ai.infra.capability.persistence.mapper.AiCapabilityMapper;
import com.thundax.kuzhambu.common.core.id.SnowflakeIdGenerator;
import java.time.Instant;
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
        return AiCapabilityMappingPersistenceAssembler.toDomain(aiCapabilityMapper.selectMapping(scope, capability));
    }

    @Override
    public List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled) {
        return AiCapabilityMappingPersistenceAssembler.toDomainList(
                aiCapabilityMapper.selectMappings(scope, capability, enabled));
    }

    @Override
    public List<AiCapabilityMapping> listMappingsByModelId(Long modelId) {
        return AiCapabilityMappingPersistenceAssembler.toDomainList(
                aiCapabilityMapper.selectMappingsByModelId(modelId));
    }

    @Override
    public Long insertMapping(AiCapabilityMapping mapping) {
        AiCapabilityMappingDO dataObject = AiCapabilityMappingPersistenceAssembler.toObject(mapping);
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
        return aiCapabilityMapper.updateMapping(AiCapabilityMappingPersistenceAssembler.toObject(mapping));
    }

    @Override
    public AiActionStatus getActionStatus(String scope, String capability) {
        return AiActionStatusPersistenceAssembler.toDomain(aiCapabilityMapper.selectActionStatus(scope, capability));
    }

    @Override
    public List<AiActionStatus> listActionStatuses(String scope, String capability, Boolean available) {
        return AiActionStatusPersistenceAssembler.toDomainList(
                aiCapabilityMapper.selectActionStatuses(scope, capability, available));
    }

    @Override
    public Long insertActionStatus(AiActionStatus actionStatus) {
        AiActionStatusDO dataObject = AiActionStatusPersistenceAssembler.toObject(actionStatus);
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
        return aiCapabilityMapper.updateActionStatus(AiActionStatusPersistenceAssembler.toObject(actionStatus));
    }

    private Long nextId() {
        return idGenerator.nextId().value();
    }
}
