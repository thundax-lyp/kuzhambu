package com.thundax.kuzhambu.ai.application.capability.service.impl;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiCapabilityApplicationServiceImpl implements AiCapabilityApplicationService {

    @Override
    public AiBusinessCapability getCapability(String capability) {
        return isBlank(capability) ? null : AiBusinessCapability.from(capability);
    }

    @Override
    public List<AiBusinessCapability> listCapabilities(Boolean enabled) {
        if (Boolean.FALSE.equals(enabled)) {
            return List.of();
        }
        return Arrays.asList(AiBusinessCapability.values());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
