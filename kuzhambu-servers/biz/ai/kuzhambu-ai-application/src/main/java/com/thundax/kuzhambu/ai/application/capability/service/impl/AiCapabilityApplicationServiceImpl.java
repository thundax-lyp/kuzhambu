package com.thundax.kuzhambu.ai.application.capability.service.impl;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapability;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiCapabilityApplicationServiceImpl implements AiCapabilityApplicationService {

    private static final String ACTION_UNAVAILABLE_NO_MAPPING = "No enabled capability mapping";
    private static final String ACTION_UNAVAILABLE_MODEL_MISMATCH = "Mapped model does not satisfy capability tags";

    private final AiCapabilityRepository aiCapabilityRepository;
    private final AiModelRepository aiModelRepository;

    public AiCapabilityApplicationServiceImpl(
            AiCapabilityRepository aiCapabilityRepository, AiModelRepository aiModelRepository) {
        this.aiCapabilityRepository = aiCapabilityRepository;
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    public AiCapability getCapability(String capability) {
        return isBlank(capability) ? null : aiCapabilityRepository.getCapability(capability);
    }

    @Override
    public List<AiCapability> listCapabilities(Boolean enabled) {
        return aiCapabilityRepository.listCapabilities(enabled);
    }

    @Override
    public AiCapabilityMapping getMapping(String scope, String capability) {
        if (isBlank(scope) || isBlank(capability)) {
            return null;
        }
        return aiCapabilityRepository.getMapping(scope, capability);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMapping(AiCapabilityMappingSaveCommand command) {
        if (command == null) {
            throw new BizException("Capability mapping command can not be null");
        }
        AiCapabilityMapping mapping = command.toEntity();
        validateMapping(mapping);
        Long mappingId =
                mapping.getMappingId() == null ? aiCapabilityRepository.saveMapping(mapping) : updateMapping(mapping);
        refreshActionStatus(mapping.getScope(), mapping.getCapability());
        return mappingId;
    }

    @Override
    public void assertModelCanBeDeleted(Long modelId) {
        if (modelId == null) {
            return;
        }
        List<AiCapabilityMapping> mappings = aiCapabilityRepository.listMappingsByModelId(modelId);
        if (mappings != null && !mappings.isEmpty()) {
            throw new BizException("Model is still used by AI capability mappings: " + modelId);
        }
    }

    @Override
    public AiActionStatusResult getActionStatus(String scope, String capability) {
        if (isBlank(scope) || isBlank(capability)) {
            return null;
        }
        return AiActionStatusResult.from(aiCapabilityRepository.getActionStatus(scope, capability));
    }

    @Override
    public List<AiActionStatusResult> listActionStatuses(String scope, String capability, Boolean available) {
        return aiCapabilityRepository.listActionStatuses(scope, capability, available).stream()
                .map(AiActionStatusResult::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiActionStatusResult refreshActionStatus(String scope, String capability) {
        if (isBlank(scope) || isBlank(capability)) {
            throw new BizException("AI action scope and capability can not be blank");
        }
        AiActionStatus current = aiCapabilityRepository.getActionStatus(scope, capability);
        AiActionStatus refreshed = buildActionStatus(current, scope, capability, Instant.now());
        if (current == null) {
            aiCapabilityRepository.saveActionStatus(refreshed);
        } else {
            aiCapabilityRepository.updateActionStatus(refreshed);
        }
        return AiActionStatusResult.from(refreshed);
    }

    private long updateMapping(AiCapabilityMapping mapping) {
        int affectedRows = aiCapabilityRepository.updateMapping(mapping);
        if (affectedRows <= 0) {
            throw new BizException("Capability mapping update failed: " + mapping.getMappingId());
        }
        return mapping.getMappingId();
    }

    private void validateMapping(AiCapabilityMapping mapping) {
        if (mapping == null
                || isBlank(mapping.getScope())
                || isBlank(mapping.getCapability())
                || mapping.getModelId() == null) {
            throw new BizException("Capability mapping scope, capability and modelId are required");
        }
        AiCapability capability = aiCapabilityRepository.getCapability(mapping.getCapability());
        AiModel model = aiModelRepository.getModelByModelId(mapping.getModelId());
        if (!mapping.canUse(capability, model)) {
            throw new BizException("Model capability tags do not satisfy AI capability: " + mapping.getCapability());
        }
    }

    private AiActionStatus buildActionStatus(
            AiActionStatus current, String scope, String capabilityName, Instant checkedAt) {
        AiCapabilityMapping mapping = aiCapabilityRepository.getMapping(scope, capabilityName);
        AiCapability capability = aiCapabilityRepository.getCapability(capabilityName);
        AiModel model = mapping == null ? null : aiModelRepository.getModelByModelId(mapping.getModelId());
        Long actionStatusId = current == null ? null : current.getActionStatusId();
        if (mapping == null || !mapping.isEnabled()) {
            return AiActionStatus.unavailable(
                    actionStatusId, scope, capabilityName, ACTION_UNAVAILABLE_NO_MAPPING, checkedAt);
        }
        if (!mapping.canUse(capability, model)) {
            return AiActionStatus.unavailable(
                    actionStatusId, scope, capabilityName, ACTION_UNAVAILABLE_MODEL_MISMATCH, checkedAt);
        }
        return AiActionStatus.available(actionStatusId, scope, capabilityName, checkedAt);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
