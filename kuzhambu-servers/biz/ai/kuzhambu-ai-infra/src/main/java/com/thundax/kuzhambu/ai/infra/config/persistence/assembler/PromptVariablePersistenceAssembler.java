package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVariableIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVariableDO;
import java.util.ArrayList;
import java.util.List;

public final class PromptVariablePersistenceAssembler {

    private PromptVariablePersistenceAssembler() {}

    public static PromptVariableDO toObject(PromptVariable variable) {
        if (variable == null) {
            return null;
        }
        PromptVariableDO dataObject = new PromptVariableDO();
        dataObject.setId(PromptVariableIdCodec.toValue(variable.getId()));
        dataObject.setTemplateId(PromptTemplateIdCodec.toValue(variable.getTemplateId()));
        dataObject.setVariableName(variable.getVariableName());
        dataObject.setRequired(variable.isRequired());
        dataObject.setDescription(variable.getDescription());
        dataObject.setPriority(variable.getPriority());
        return dataObject;
    }

    public static PromptVariable toDomain(PromptVariableDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptVariable(
                PromptVariableIdCodec.toDomain(dataObject.getId()),
                PromptTemplateIdCodec.toDomain(dataObject.getTemplateId()),
                dataObject.getVariableName(),
                Boolean.TRUE.equals(dataObject.getRequired()),
                dataObject.getDescription(),
                dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    public static List<PromptVariable> toDomainList(List<PromptVariableDO> dataObjects) {
        List<PromptVariable> variables = new ArrayList<>();
        if (dataObjects == null) {
            return variables;
        }
        for (PromptVariableDO dataObject : dataObjects) {
            variables.add(toDomain(dataObject));
        }
        return variables;
    }
}
