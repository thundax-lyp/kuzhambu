package com.thundax.kuzhambu.ai.domain.config.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import java.util.List;

public interface AiBusinessConfigRepository {

    AiBusinessConfig get(AiBusinessConfigId id);

    AiBusinessConfig get(AiBusinessCapability capability);

    List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled);

    AiBusinessConfigId insert(AiBusinessConfig config);

    int update(AiBusinessConfig config);

    int maxPriority();

    int delete(AiBusinessConfigId id);
}
