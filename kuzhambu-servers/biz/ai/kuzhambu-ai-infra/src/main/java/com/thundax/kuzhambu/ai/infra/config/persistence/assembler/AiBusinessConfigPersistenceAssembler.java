package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.config.codec.AiBusinessConfigIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.AiBusinessConfigDO;
import java.util.ArrayList;
import java.util.List;

public final class AiBusinessConfigPersistenceAssembler {

    private AiBusinessConfigPersistenceAssembler() {}

    public static AiBusinessConfigDO toObject(AiBusinessConfig config) {
        if (config == null) {
            return null;
        }
        return new AiBusinessConfigDO(
                AiBusinessConfigIdCodec.toValue(config.getId()),
                config.getCapability() == null ? null : config.getCapability().value(),
                PromptTemplateIdCodec.toValue(config.getPromptTemplateId()),
                AiModelIdCodec.toValue(config.getModelId()),
                config.getDefaultParamsJson(),
                config.isEnabled(),
                config.getConfiguredAt());
    }

    public static AiBusinessConfig toDomain(AiBusinessConfigDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiBusinessConfig(
                AiBusinessConfigIdCodec.toDomain(dataObject.getId()),
                AiBusinessCapability.from(dataObject.getCapability()),
                PromptTemplateIdCodec.toDomain(dataObject.getPromptTemplateId()),
                AiModelIdCodec.toDomain(dataObject.getModelId()),
                dataObject.getDefaultParamsJson(),
                Boolean.TRUE.equals(dataObject.getEnabled()),
                dataObject.getConfiguredAt());
    }

    public static List<AiBusinessConfig> toDomainList(List<AiBusinessConfigDO> dataObjects) {
        List<AiBusinessConfig> configs = new ArrayList<>();
        if (dataObjects == null) {
            return configs;
        }
        for (AiBusinessConfigDO dataObject : dataObjects) {
            configs.add(toDomain(dataObject));
        }
        return configs;
    }
}
