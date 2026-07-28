package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import java.util.List;

public interface AiBusinessConfigApplicationService {

    AiBusinessConfig get(Long id);

    AiBusinessConfig get(String capability);

    List<AiBusinessConfig> list(String capability, Boolean enabled);

    Long save(AiBusinessConfig config);

    int update(AiBusinessConfig config);

    int delete(Long id);
}
