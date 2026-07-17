package com.thundax.kuzhambu.ai.application.config.model.service.impl;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class AiModelApplicationServiceImpl implements AiModelApplicationService {

    private final AiModelRepository aiModelRepository;
    private final AiCapabilityApplicationService aiCapabilityApplicationService;

    public AiModelApplicationServiceImpl(
            AiModelRepository aiModelRepository, AiCapabilityApplicationService aiCapabilityApplicationService) {
        this.aiModelRepository = aiModelRepository;
        this.aiCapabilityApplicationService = aiCapabilityApplicationService;
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
        int affectedRows = aiModelRepository.update(model);
        if (affectedRows > 0) {
            aiCapabilityApplicationService.refreshActionStatusesByModelId(AiModelIdCodec.toValue(model.getId()));
        }
        return affectedRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        if (id == null) {
            return 0;
        }
        aiCapabilityApplicationService.assertModelCanBeDeleted(id);
        return aiModelRepository.delete(AiModelIdCodec.toDomain(id));
    }
}
