package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.service.AiCapabilityCatalogApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiCapabilityCatalogApplicationServiceImpl implements AiCapabilityCatalogApplicationService {

    @Override
    public AiBusinessCapability getCapability(AiBusinessCapability capability) {
        return capability;
    }

    @Override
    public List<AiBusinessCapability> listCapabilities(Boolean enabled) {
        if (Boolean.FALSE.equals(enabled)) {
            return List.of();
        }
        return Arrays.asList(AiBusinessCapability.values());
    }
}
