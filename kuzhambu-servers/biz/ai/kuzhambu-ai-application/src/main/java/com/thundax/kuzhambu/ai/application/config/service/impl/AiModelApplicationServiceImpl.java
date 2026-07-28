package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
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
    public AiModel get(AiModelId id) {
        return aiModelRepository.get(id);
    }

    @Override
    public List<AiModel> list(AiApiSource apiSource, Boolean enabled) {
        return aiModelRepository.list(apiSource == null ? null : apiSource.value(), enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelId save(AiModel model) {
        if (model == null) {
            return null;
        }
        return aiModelRepository.insert(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AiModel model) {
        if (model == null) {
            return 0;
        }
        return aiModelRepository.update(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(AiModelId id) {
        if (id == null) {
            return 0;
        }
        assertModelCanBeDeleted(id);
        return aiModelRepository.delete(id);
    }

    private void assertModelCanBeDeleted(AiModelId modelId) {
        for (AiBusinessConfig config : aiBusinessConfigRepository.list(null, null)) {
            if (config.getModelId() != null && config.getModelId().equals(modelId)) {
                throw new BizException("AI model is used by business config");
            }
        }
    }
}
