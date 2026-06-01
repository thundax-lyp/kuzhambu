package com.thundax.kuzhambu.ai.application.capability.service;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapability;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import java.util.List;

public interface AiCapabilityApplicationService {

    AiCapability getCapability(String capability);

    List<AiCapability> listCapabilities(Boolean enabled);

    AiCapabilityMapping getMapping(String scope, String capability);

    Long saveMapping(AiCapabilityMappingSaveCommand command);

    void assertModelCanBeDeleted(Long modelId);

    AiActionStatusResult getActionStatus(String scope, String capability);

    AiActionStatusResult refreshActionStatus(String scope, String capability);
}
