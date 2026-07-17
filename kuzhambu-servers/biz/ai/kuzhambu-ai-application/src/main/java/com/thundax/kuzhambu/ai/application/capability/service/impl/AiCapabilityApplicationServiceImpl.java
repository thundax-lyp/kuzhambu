package com.thundax.kuzhambu.ai.application.capability.service.impl;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiCapabilityApplicationServiceImpl implements AiCapabilityApplicationService {

    private static final String ACTION_UNAVAILABLE_NO_MAPPING = "No enabled capability mapping";
    private static final String ACTION_UNAVAILABLE_MODEL_MISMATCH = "Mapped model does not satisfy capability tags";
    private static final String ACTION_UNAVAILABLE_SERVICE = "Mapped service is unavailable";

    private final AiCapabilityRepository aiCapabilityRepository;
    private final AiModelRepository aiModelRepository;

    public AiCapabilityApplicationServiceImpl(
            AiCapabilityRepository aiCapabilityRepository, AiModelRepository aiModelRepository) {
        this.aiCapabilityRepository = aiCapabilityRepository;
        this.aiModelRepository = aiModelRepository;
    }

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

    @Override
    public AiCapabilityMapping getMapping(String scope, String capability) {
        if (isBlank(scope) || isBlank(capability)) {
            return null;
        }
        return aiCapabilityRepository.getMapping(scope, capability);
    }

    @Override
    public List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled) {
        return aiCapabilityRepository.listMappings(scope, capability, enabled);
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
    @Transactional(rollbackFor = Exception.class)
    public void refreshActionStatusesByModelId(Long modelId) {
        if (modelId == null) {
            return;
        }
        List<AiCapabilityMapping> mappings = aiCapabilityRepository.listMappingsByModelId(modelId);
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        for (AiCapabilityMapping mapping : mappings) {
            refreshActionStatus(mapping.getScope(), mapping.getCapability());
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
        if (!mapping.isEnabled()) {
            return;
        }
        AiBusinessCapability.from(mapping.getCapability());
        AiModel model = aiModelRepository.getModelById(AiModelId.of(mapping.getModelId()));
        if (!mapping.canUse(model)) {
            throw new BizException("Model capability tags do not satisfy AI capability: " + mapping.getCapability());
        }
    }

    private AiActionStatus buildActionStatus(
            AiActionStatus current, String scope, String capabilityName, Instant checkedAt) {
        AiCapabilityMapping mapping = aiCapabilityRepository.getMapping(scope, capabilityName);
        AiModel model = mapping == null ? null : aiModelRepository.getModelById(AiModelId.of(mapping.getModelId()));
        Long actionStatusId = current == null ? null : current.getActionStatusId();
        if (mapping == null || !mapping.isEnabled()) {
            return AiActionStatus.unavailable(
                    actionStatusId, scope, capabilityName, ACTION_UNAVAILABLE_NO_MAPPING, checkedAt);
        }
        if (!mapping.canUse(model)) {
            return AiActionStatus.unavailable(
                    actionStatusId, scope, capabilityName, ACTION_UNAVAILABLE_MODEL_MISMATCH, checkedAt);
        }
        if (model.getApiSource() == null || isBlank(model.getBaseUrl())) {
            return AiActionStatus.unavailable(
                    actionStatusId, scope, capabilityName, ACTION_UNAVAILABLE_SERVICE, checkedAt);
        }
        return AiActionStatus.available(actionStatusId, scope, capabilityName, checkedAt);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
