package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.PromptTemplateStatus;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptTemplateDO;

public final class PromptTemplatePersistenceAssembler {

    private PromptTemplatePersistenceAssembler() {}

    public static PromptTemplateDO toObject(PromptTemplate template) {
        if (template == null) {
            return null;
        }
        PromptTemplateDO dataObject = new PromptTemplateDO();
        dataObject.setId(PromptTemplateIdCodec.toValue(template.getId()));
        dataObject.setCapability(
                template.getCapability() == null
                        ? null
                        : template.getCapability().value());
        dataObject.setName(template.getName());
        dataObject.setDescription(template.getDescription());
        dataObject.setStatus(
                template.getStatus() == null ? null : template.getStatus().value());
        dataObject.setCurrentVersionNo(template.getCurrentVersionNo());
        dataObject.setRegisteredAt(template.getRegisteredAt());
        return dataObject;
    }

    public static PromptTemplate toDomain(PromptTemplateDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptTemplate(
                PromptTemplateIdCodec.toDomain(dataObject.getId()),
                AiBusinessCapability.from(dataObject.getCapability()),
                dataObject.getName(),
                dataObject.getDescription(),
                PromptTemplateStatus.fromNullable(dataObject.getStatus()),
                dataObject.getCurrentVersionNo(),
                dataObject.getRegisteredAt());
    }
}
