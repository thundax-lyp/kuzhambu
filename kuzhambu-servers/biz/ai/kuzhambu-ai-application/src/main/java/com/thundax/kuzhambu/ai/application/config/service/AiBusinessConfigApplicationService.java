package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import java.util.List;

public interface AiBusinessConfigApplicationService {

    AiBusinessConfig get(AiBusinessConfigId id);

    AiBusinessConfig get(AiBusinessCapability capability);

    List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled);

    AiBusinessConfigId save(AiBusinessConfig config);

    int update(AiBusinessConfig config);

    int delete(AiBusinessConfigId id);
}
