package com.thundax.kuzhambu.ai.domain.config.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.List;

public interface AiModelRepository {

    AiModel getModelById(AiModelId id);

    List<AiModel> listModels(String apiSource, Boolean enabled);

    AiModelId saveModel(AiModel model);

    int updateModel(AiModel model);

    int deleteModel(AiModelId id);
}
