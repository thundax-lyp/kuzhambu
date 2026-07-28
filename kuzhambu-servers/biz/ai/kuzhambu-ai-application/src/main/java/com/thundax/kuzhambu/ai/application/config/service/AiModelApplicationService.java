package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.List;

public interface AiModelApplicationService {

    AiModel get(AiModelId id);

    List<AiModel> list(AiApiSource apiSource, Boolean enabled);

    AiModelId save(AiModel model);

    int update(AiModel model);

    int delete(AiModelId id);
}
