package com.thundax.kuzhambu.ai.application.config.service;

import com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import java.util.List;

public interface AiBusinessConfigApplicationService {

    AiBusinessConfig get(GetAiBusinessConfigQuery query);

    AiBusinessConfig getByCapability(GetAiBusinessConfigByCapabilityQuery query);

    List<AiBusinessConfig> list(ListAiBusinessConfigsQuery query);

    AiBusinessConfigId create(CreateAiBusinessConfigCommand command);

    int update(UpdateAiBusinessConfigCommand command);

    int delete(DeleteAiBusinessConfigCommand command);
}
