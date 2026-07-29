package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiModelQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiModelsQuery;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import java.util.List;

public interface AiModelApplicationService {

    AiModel get(GetAiModelQuery query);

    List<AiModel> list(ListAiModelsQuery query);

    AiModelId create(CreateAiModelCommand command);

    int update(UpdateAiModelCommand command);

    int delete(DeleteAiModelCommand command);
}
