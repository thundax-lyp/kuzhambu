package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.application.config.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiBusinessConfigApplicationServiceImpl implements AiBusinessConfigApplicationService {

    private final AiBusinessConfigRepository aiBusinessConfigRepository;
    private final PromptRepository promptRepository;
    private final AiModelRepository aiModelRepository;

    public AiBusinessConfigApplicationServiceImpl(
            AiBusinessConfigRepository aiBusinessConfigRepository,
            PromptRepository promptRepository,
            AiModelRepository aiModelRepository) {
        this.aiBusinessConfigRepository = aiBusinessConfigRepository;
        this.promptRepository = promptRepository;
        this.aiModelRepository = aiModelRepository;
    }

    @Override
    public AiBusinessConfig get(GetAiBusinessConfigQuery query) {
        return aiBusinessConfigRepository.get(query == null ? null : query.businessConfigId());
    }

    @Override
    public AiBusinessConfig getByCapability(GetAiBusinessConfigByCapabilityQuery query) {
        return aiBusinessConfigRepository.get(query == null ? null : query.capability());
    }

    @Override
    public List<AiBusinessConfig> list(ListAiBusinessConfigsQuery query) {
        return aiBusinessConfigRepository.list(
                query == null ? null : query.capability(), query == null ? null : query.enabled());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiBusinessConfigId create(CreateAiBusinessConfigCommand command) {
        AiBusinessConfig config = toConfig(command);
        validateConfig(config);
        if (config.getId() == null) {
            config.setPriority(aiBusinessConfigRepository.maxPriority() + 1);
        } else {
            AiBusinessConfig existing = aiBusinessConfigRepository.get(config.getId());
            config.setPriority(
                    existing == null ? aiBusinessConfigRepository.maxPriority() + 1 : existing.getPriority());
        }
        return aiBusinessConfigRepository.insert(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateAiBusinessConfigCommand command) {
        AiBusinessConfig config = toConfig(command);
        if (config == null || config.getId() == null) {
            throw new BizException("AI business config id is required");
        }
        AiBusinessConfig existing = aiBusinessConfigRepository.get(config.getId());
        if (existing != null && existing.getCapability() != config.getCapability()) {
            throw new BizException("AI business config capability cannot be changed");
        }
        validateConfig(config);
        config.setPriority(existing == null ? aiBusinessConfigRepository.maxPriority() + 1 : existing.getPriority());
        return aiBusinessConfigRepository.update(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(DeleteAiBusinessConfigCommand command) {
        AiBusinessConfigId businessConfigId = command == null ? null : command.businessConfigId();
        if (businessConfigId == null) {
            return 0;
        }
        return aiBusinessConfigRepository.delete(businessConfigId);
    }

    private void validateConfig(AiBusinessConfig config) {
        if (config == null
                || config.getCapability() == null
                || config.getPromptTemplateId() == null
                || config.getModelId() == null) {
            throw new BizException("AI business capability, prompt template and model are required");
        }
        PromptTemplate promptTemplate = promptRepository.get(config.getPromptTemplateId());
        AiModel model = aiModelRepository.get(config.getModelId());
        if (!config.promptMatches(promptTemplate) || !config.modelMatches(model)) {
            throw new BizException("AI business config does not match prompt template or model capability");
        }
    }

    private AiBusinessConfig toConfig(CreateAiBusinessConfigCommand command) {
        if (command == null) {
            return null;
        }
        AiBusinessConfig config = new AiBusinessConfig();
        config.setId(command.getId());
        config.setCapability(command.getCapability());
        config.setPromptTemplateId(command.getPromptTemplateId());
        config.setModelId(command.getModelId());
        config.setDefaultParamsJson(command.getDefaultParamsJson());
        config.setEnabled(command.getEnabled() == null || command.getEnabled());
        config.setConfiguredAt(Instant.now());
        return config;
    }

    private AiBusinessConfig toConfig(UpdateAiBusinessConfigCommand command) {
        if (command == null) {
            return null;
        }
        AiBusinessConfig config = new AiBusinessConfig();
        config.setId(command.getId());
        config.setCapability(command.getCapability());
        config.setPromptTemplateId(command.getPromptTemplateId());
        config.setModelId(command.getModelId());
        config.setDefaultParamsJson(command.getDefaultParamsJson());
        config.setEnabled(command.getEnabled() == null || command.getEnabled());
        config.setConfiguredAt(Instant.now());
        return config;
    }
}
