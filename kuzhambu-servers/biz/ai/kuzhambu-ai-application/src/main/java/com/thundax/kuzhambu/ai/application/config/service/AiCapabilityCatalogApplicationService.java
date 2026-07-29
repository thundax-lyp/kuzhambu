package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.application.config.query.GetAiCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiCapabilitiesQuery;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import java.util.List;

public interface AiCapabilityCatalogApplicationService {

    AiBusinessCapability get(GetAiCapabilityQuery query);

    List<AiBusinessCapability> list(ListAiCapabilitiesQuery query);
}
