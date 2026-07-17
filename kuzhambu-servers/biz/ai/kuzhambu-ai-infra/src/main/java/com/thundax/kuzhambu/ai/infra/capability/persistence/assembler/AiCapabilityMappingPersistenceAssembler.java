package com.thundax.kuzhambu.ai.infra.capability.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiCapabilityMappingDO;
import java.util.ArrayList;
import java.util.List;

public final class AiCapabilityMappingPersistenceAssembler {

    private AiCapabilityMappingPersistenceAssembler() {}

    public static AiCapabilityMappingDO toObject(AiCapabilityMapping mapping) {
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

    public static AiCapabilityMapping toDomain(AiCapabilityMappingDO dataObject) {
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

    public static List<AiCapabilityMapping> toDomainList(List<AiCapabilityMappingDO> dataObjects) {
        List<AiCapabilityMapping> mappings = new ArrayList<>();
        if (dataObjects == null) {
            return mappings;
        }
        for (AiCapabilityMappingDO dataObject : dataObjects) {
            mappings.add(toDomain(dataObject));
        }
        return mappings;
    }
}
