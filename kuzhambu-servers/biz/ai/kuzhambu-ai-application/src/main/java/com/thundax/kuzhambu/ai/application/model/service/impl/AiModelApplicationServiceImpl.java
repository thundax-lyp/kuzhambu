package com.thundax.kuzhambu.ai.application.model.service.impl;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.model.command.AiModelCheckCommand;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
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

    public AiModelApplicationServiceImpl(
            AiModelRepository aiModelRepository, AiCapabilityApplicationService aiCapabilityApplicationService) {
        this.aiModelRepository = aiModelRepository;
        this.aiCapabilityApplicationService = aiCapabilityApplicationService;
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
}
