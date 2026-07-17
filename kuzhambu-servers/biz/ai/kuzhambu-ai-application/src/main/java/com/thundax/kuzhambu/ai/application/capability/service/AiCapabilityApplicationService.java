package com.thundax.kuzhambu.ai.application.capability.service;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.List;

public interface AiCapabilityApplicationService {

    AiBusinessCapability getCapability(String capability);

    List<AiBusinessCapability> listCapabilities(Boolean enabled);
}
