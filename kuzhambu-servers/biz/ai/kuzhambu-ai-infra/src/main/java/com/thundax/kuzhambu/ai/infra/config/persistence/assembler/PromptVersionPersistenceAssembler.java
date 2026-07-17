package com.thundax.kuzhambu.ai.infra.config.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.infra.config.persistence.dataobject.PromptVersionDO;
import java.util.ArrayList;
import java.util.List;

public final class PromptVersionPersistenceAssembler {

    private PromptVersionPersistenceAssembler() {}

    public static PromptVersionDO toObject(PromptVersion version) {
        if (version == null) {
            return null;
        }
        PromptVersionDO dataObject = new PromptVersionDO();
        dataObject.setId(PromptVersionIdCodec.toValue(version.getId()));
        dataObject.setTemplateId(PromptTemplateIdCodec.toValue(version.getTemplateId()));
        dataObject.setVersionNo(version.getVersionNo());
        dataObject.setMessageTemplatesJson(version.getMessageTemplatesJson());
        dataObject.setVariablesSnapshotJson(version.getVariablesSnapshotJson());
        dataObject.setOutputSchemaJson(version.getOutputSchemaJson());
        dataObject.setChangeSummary(version.getChangeSummary());
        dataObject.setRegisteredAt(version.getRegisteredAt());
        return dataObject;
    }

    public static PromptVersion toDomain(PromptVersionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new PromptVersion(
                PromptVersionIdCodec.toDomain(dataObject.getId()),
                PromptTemplateIdCodec.toDomain(dataObject.getTemplateId()),
                dataObject.getVersionNo() == null ? 0 : dataObject.getVersionNo(),
                dataObject.getMessageTemplatesJson(),
                dataObject.getVariablesSnapshotJson(),
                dataObject.getOutputSchemaJson(),
                dataObject.getChangeSummary(),
                dataObject.getRegisteredAt());
    }

    public static List<PromptVersion> toDomainList(List<PromptVersionDO> dataObjects) {
        List<PromptVersion> versions = new ArrayList<>();
        if (dataObjects == null) {
            return versions;
        }
        for (PromptVersionDO dataObject : dataObjects) {
            versions.add(toDomain(dataObject));
        }
        return versions;
    }
}
