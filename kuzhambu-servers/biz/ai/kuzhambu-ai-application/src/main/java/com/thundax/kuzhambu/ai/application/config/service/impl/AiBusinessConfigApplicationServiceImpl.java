package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiBusinessConfigIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
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
    public AiBusinessConfig get(Long id) {
        return aiBusinessConfigRepository.get(AiBusinessConfigIdCodec.toDomain(id));
    }

    @Override
    public AiBusinessConfig get(String capability) {
        return aiBusinessConfigRepository.get(toCapability(capability));
    }

    @Override
    public List<AiBusinessConfig> list(String capability, Boolean enabled) {
        return aiBusinessConfigRepository.list(toCapability(capability), enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiBusinessConfig config) {
        validateConfig(config);
        if (config.getId() == null) {
            config.setPriority(aiBusinessConfigRepository.maxPriority() + 1);
        } else {
            AiBusinessConfig existing = aiBusinessConfigRepository.get(config.getId());
            config.setPriority(
                    existing == null ? aiBusinessConfigRepository.maxPriority() + 1 : existing.getPriority());
        }
        AiBusinessConfigId id = aiBusinessConfigRepository.insert(config);
        return AiBusinessConfigIdCodec.toValue(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AiBusinessConfig config) {
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
    public int delete(Long id) {
        if (id == null) {
            return 0;
        }
        return aiBusinessConfigRepository.delete(AiBusinessConfigIdCodec.toDomain(id));
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

    private AiBusinessCapability toCapability(String capability) {
        if (capability == null || capability.trim().isEmpty()) {
            return null;
        }
        return AiBusinessCapability.from(capability);
    }
}
