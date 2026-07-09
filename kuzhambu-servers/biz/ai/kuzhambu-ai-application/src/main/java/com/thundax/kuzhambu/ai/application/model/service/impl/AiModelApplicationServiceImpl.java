package com.thundax.kuzhambu.ai.application.model.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.model.command.AiModelCheckCommand;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiModelApplicationServiceImpl implements AiModelApplicationService {

    private final AiModelRepository aiModelRepository;
    private final AiCapabilityApplicationService aiCapabilityApplicationService;
    private final AiServiceConfigApplicationService aiServiceConfigApplicationService;
    private final ObjectMapper objectMapper;

    public AiModelApplicationServiceImpl(
            AiModelRepository aiModelRepository,
            AiCapabilityApplicationService aiCapabilityApplicationService,
            AiServiceConfigApplicationService aiServiceConfigApplicationService,
            ObjectMapper objectMapper) {
        this.aiModelRepository = aiModelRepository;
        this.aiCapabilityApplicationService = aiCapabilityApplicationService;
        this.aiServiceConfigApplicationService = aiServiceConfigApplicationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiModel get(Long modelId) {
        return modelId == null ? null : aiModelRepository.getModelByModelId(modelId);
    }

    @Override
    public List<AiModel> list(Long serviceId, Boolean enabled) {
        return aiModelRepository.listModels(serviceId, enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiModel model) {
        if (model == null) {
            return null;
        }
        return aiModelRepository.saveModel(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AiModel model) {
        if (model == null) {
            return 0;
        }
        return aiModelRepository.updateModel(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long modelId) {
        if (modelId == null) {
            return 0;
        }
        aiCapabilityApplicationService.assertModelCanBeDeleted(modelId);
        return aiModelRepository.deleteModel(modelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelCheckRecord check(Long modelId) {
        Instant startedAt = Instant.now();
        AiModel model = get(modelId);
        if (model == null) {
            throw new BizException("AI-MODEL-404", "ai.model.not-found", "AI model not found: " + modelId);
        }

        AiModelCheckRecord record = buildCheckRecord(model, startedAt);
        record.setCheckId(aiModelRepository.insertCheckRecord(record));
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordCheck(AiModelCheckCommand command) {
        if (command == null) {
            return null;
        }
        return aiModelRepository.insertCheckRecord(command.toRecord());
    }

    @Override
    public List<AiModelCheckRecord> listCheckRecords(Long modelId) {
        if (modelId == null) {
            return Collections.emptyList();
        }
        return aiModelRepository.listCheckRecords(modelId);
    }

    private AiModelCheckRecord buildCheckRecord(AiModel model, Instant startedAt) {
        AiModelCheckRecord record = new AiModelCheckRecord();
        record.setModelId(model.getModelId());
        record.setServiceId(model.getServiceId());
        record.setModelName(model.getModelName());
        record.setCheckedAt(Instant.now());
        record.setLatencyMs(
                (int) Duration.between(startedAt, record.getCheckedAt()).toMillis());

        try {
            validateModelForCheck(model);
            record.setStatus("SUCCEEDED");
        } catch (RuntimeException ex) {
            record.setStatus("FAILED");
            record.setErrorType(
                    ex instanceof BizException bizException ? bizException.getCode() : "MODEL_CHECK_FAILED");
            record.setErrorMessage(ex.getMessage());
        }
        return record;
    }

    private void validateModelForCheck(AiModel model) {
        if (!model.isEnabled()) {
            throw new BizException(
                    "AI-MODEL-DISABLED", "ai.model.disabled", "AI model is disabled: " + model.getModelId());
        }
        AiServiceConfig serviceConfig = aiServiceConfigApplicationService.getByServiceId(model.getServiceId());
        if (serviceConfig == null) {
            throw new BizException(
                    "AI-SERVICE-404", "ai.service.not-found", "AI service config not found: " + model.getServiceId());
        }
        if (!serviceConfig.isAvailable()) {
            throw new BizException(
                    "AI-SERVICE-UNAVAILABLE",
                    "ai.service.unavailable",
                    "AI service is unavailable: " + serviceConfig.getServiceId());
        }
        if (model.getDefaultParamsJson() != null
                && !model.getDefaultParamsJson().isBlank()) {
            try {
                objectMapper.readTree(model.getDefaultParamsJson());
            } catch (JsonProcessingException ex) {
                throw new BizException(
                        "AI-MODEL-PARAMS-INVALID",
                        "ai.model.params.invalid",
                        "AI model default parameters is not valid JSON",
                        ex);
            }
        }
    }
}
