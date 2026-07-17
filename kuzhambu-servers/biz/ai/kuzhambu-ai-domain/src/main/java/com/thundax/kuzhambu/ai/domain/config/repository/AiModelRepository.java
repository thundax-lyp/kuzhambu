package com.thundax.kuzhambu.ai.domain.config.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.List;

public interface AiModelRepository {

    AiModel get(AiModelId id);

    List<AiModel> list(String apiSource, Boolean enabled);

    AiModelId insert(AiModel model);

    int update(AiModel model);

    int delete(AiModelId id);
}
