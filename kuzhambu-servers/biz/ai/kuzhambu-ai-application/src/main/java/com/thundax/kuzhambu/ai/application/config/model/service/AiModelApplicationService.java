package com.thundax.kuzhambu.ai.application.config.model.service;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import java.util.List;

public interface AiModelApplicationService {

    AiModel get(Long id);

    List<AiModel> list(String apiSource, Boolean enabled);

    Long save(AiModel model);

    int update(AiModel model);

    int delete(Long id);
}
