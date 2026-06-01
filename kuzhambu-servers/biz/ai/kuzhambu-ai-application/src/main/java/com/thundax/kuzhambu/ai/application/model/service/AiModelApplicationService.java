package com.thundax.kuzhambu.ai.application.model.service;

import com.thundax.kuzhambu.ai.application.model.command.AiModelCheckCommand;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import java.util.List;

public interface AiModelApplicationService {

    AiModel get(Long modelId);

    List<AiModel> list(Long serviceId, Boolean enabled);

    Long save(AiModel model);

    int update(AiModel model);

    int delete(Long modelId);

    Long recordCheck(AiModelCheckCommand command);

    List<AiModelCheckRecord> listCheckRecords(Long modelId);
}
