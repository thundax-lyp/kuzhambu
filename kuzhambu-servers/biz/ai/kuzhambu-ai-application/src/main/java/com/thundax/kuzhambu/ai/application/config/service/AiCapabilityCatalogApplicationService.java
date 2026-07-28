package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.List;

public interface AiCapabilityCatalogApplicationService {

    AiBusinessCapability getCapability(AiBusinessCapability capability);

    List<AiBusinessCapability> listCapabilities(Boolean enabled);
}
