package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiModelQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiModelsQuery;
import com.thundax.kuzhambu.ai.application.config.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiModelApplicationServiceImpl implements AiModelApplicationService {

    private final AiBusinessConfigRepository aiBusinessConfigRepository;
    private final AiModelRepository aiModelRepository;

    public AiModelApplicationServiceImpl(
            AiBusinessConfigRepository aiBusinessConfigRepository, AiModelRepository aiModelRepository) {
        this.aiBusinessConfigRepository = aiBusinessConfigRepository;
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    public AiModel get(GetAiModelQuery query) {
        return aiModelRepository.get(query == null ? null : query.modelId());
    }

    @Override
    public List<AiModel> list(ListAiModelsQuery query) {
        var apiSource = query == null ? null : query.apiSource();
        Boolean enabled = query == null ? null : query.enabled();
        return aiModelRepository.list(apiSource == null ? null : apiSource.value(), enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelId create(CreateAiModelCommand command) {
        if (command == null) {
            return null;
        }
        return aiModelRepository.insert(toModel(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateAiModelCommand command) {
        if (command == null) {
            return 0;
        }
        return aiModelRepository.update(toModel(command));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(DeleteAiModelCommand command) {
        AiModelId modelId = command == null ? null : command.modelId();
        if (modelId == null) {
            return 0;
        }
        assertModelCanBeDeleted(modelId);
        return aiModelRepository.delete(modelId);
    }

    private void assertModelCanBeDeleted(AiModelId modelId) {
        for (AiBusinessConfig config : aiBusinessConfigRepository.list(null, null)) {
            if (config.getModelId() != null && config.getModelId().equals(modelId)) {
                throw new BizException("AI model is used by business config");
            }
        }
    }

    private AiModel toModel(CreateAiModelCommand command) {
        AiModel model = new AiModel();
        model.setId(command.getId());
        model.setApiSource(command.getApiSource());
        model.setBaseUrl(command.getBaseUrl());
        model.setEncryptedApiKey(command.getEncryptedApiKey());
        model.setModelName(command.getModelName());
        model.setDisplayName(command.getDisplayName());
        model.setCapabilities(command.getCapabilities());
        model.setDefaultParamsJson(command.getDefaultParamsJson());
        model.setDescription(command.getDescription());
        model.setEnabled(command.getEnabled() == null || command.getEnabled());
        model.setRegisteredAt(Instant.now());
        return model;
    }

    private AiModel toModel(UpdateAiModelCommand command) {
        AiModel model = new AiModel();
        model.setId(command.getId());
        model.setApiSource(command.getApiSource());
        model.setBaseUrl(command.getBaseUrl());
        model.setEncryptedApiKey(command.getEncryptedApiKey());
        model.setModelName(command.getModelName());
        model.setDisplayName(command.getDisplayName());
        model.setCapabilities(command.getCapabilities());
        model.setDefaultParamsJson(command.getDefaultParamsJson());
        model.setDescription(command.getDescription());
        model.setEnabled(command.getEnabled() == null || command.getEnabled());
        model.setRegisteredAt(Instant.now());
        return model;
    }
}
