package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.query.GetAiCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiCapabilitiesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptCapabilityVariablesQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptCapabilityVariableResult;
import com.thundax.kuzhambu.ai.application.config.service.AiCapabilityCatalogApplicationService;
import com.thundax.kuzhambu.ai.application.config.support.PromptCapabilityVariableCatalog;
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
    public AiBusinessCapability get(GetAiCapabilityQuery query) {
        return query == null ? null : query.capability();
    }

    @Override
    public List<AiBusinessCapability> list(ListAiCapabilitiesQuery query) {
        if (Boolean.FALSE.equals(query == null ? null : query.getEnabled())) {
            return List.of();
        }
        return Arrays.asList(AiBusinessCapability.values());
    }

    @Override
    public List<PromptCapabilityVariableResult> listPromptVariables(ListPromptCapabilityVariablesQuery query) {
        return PromptCapabilityVariableCatalog.list(query == null ? null : query.capability());
    }
}
