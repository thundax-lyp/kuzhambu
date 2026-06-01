package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;

public interface AiServiceConfigApplicationService {

    AiServiceConfig getByServiceId(Long serviceId);

    AiServiceConfig getByRole(String serviceRole);

    Long save(AiServiceConfig serviceConfig);
}
