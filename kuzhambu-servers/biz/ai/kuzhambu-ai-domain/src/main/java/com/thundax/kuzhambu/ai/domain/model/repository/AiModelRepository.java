package com.thundax.kuzhambu.ai.domain.model.repository;

import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import java.util.List;

public interface AiModelRepository {

    AiServiceConfig getServiceConfigByServiceId(Long serviceId);

    AiServiceConfig getServiceConfigByRole(String serviceRole);

    Long saveServiceConfig(AiServiceConfig serviceConfig);

    AiModel getModelByModelId(Long modelId);

    List<AiModel> listModels(Long serviceId, Boolean enabled);

    Long saveModel(AiModel model);

    int updateModel(AiModel model);

    int deleteModel(Long modelId);

    Long insertCheckRecord(AiModelCheckRecord checkRecord);

    List<AiModelCheckRecord> listCheckRecords(Long modelId);
}
