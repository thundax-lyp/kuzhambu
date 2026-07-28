package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.thundax.kuzhambu.ai.application.config.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
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
    public AiModel get(Long id) {
        return aiModelRepository.get(AiModelIdCodec.toDomain(id));
    }

    @Override
    public List<AiModel> list(String apiSource, Boolean enabled) {
        return aiModelRepository.list(apiSource, enabled);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiModel model) {
        if (model == null) {
            return null;
        }
        AiModelId id = aiModelRepository.insert(model);
        return AiModelIdCodec.toValue(id);
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
    public int delete(Long id) {
        if (id == null) {
            return 0;
        }
        AiModelId modelId = AiModelIdCodec.toDomain(id);
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
}
