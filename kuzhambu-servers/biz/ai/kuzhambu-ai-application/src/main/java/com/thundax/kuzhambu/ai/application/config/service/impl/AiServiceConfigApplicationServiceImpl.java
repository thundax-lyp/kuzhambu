package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiServiceConfigApplicationServiceImpl implements AiServiceConfigApplicationService {

    private final AiModelRepository aiModelRepository;

    public AiServiceConfigApplicationServiceImpl(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    public AiServiceConfig getByServiceId(Long serviceId) {
        return serviceId == null ? null : aiModelRepository.getServiceConfigByServiceId(serviceId);
    }

    @Override
    public AiServiceConfig getByRole(String serviceRole) {
        return serviceRole == null ? null : aiModelRepository.getServiceConfigByRole(serviceRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiServiceConfig serviceConfig) {
        if (serviceConfig == null) {
            return null;
        }
        return aiModelRepository.saveServiceConfig(serviceConfig);
    }
}
