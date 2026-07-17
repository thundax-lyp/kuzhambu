package com.thundax.kuzhambu.ai.domain.capability.repository;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import java.util.List;

public interface AiCapabilityRepository {

    AiCapabilityMapping getMapping(String scope, String capability);

    List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled);

    List<AiCapabilityMapping> listMappingsByModelId(Long modelId);

    Long insertMapping(AiCapabilityMapping mapping);

    int updateMapping(AiCapabilityMapping mapping);

    AiActionStatus getActionStatus(String scope, String capability);

    List<AiActionStatus> listActionStatuses(String scope, String capability, Boolean available);

    Long insertActionStatus(AiActionStatus actionStatus);

    int updateActionStatus(AiActionStatus actionStatus);
}
