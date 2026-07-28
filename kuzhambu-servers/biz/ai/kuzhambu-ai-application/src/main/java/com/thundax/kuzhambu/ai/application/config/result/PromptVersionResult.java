package com.thundax.kuzhambu.ai.application.config.result;

import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromptVersionResult {

    private final Long id;
    private final Long templateId;
    private final int versionNo;
    private final String messageTemplatesJson;
    private final String variablesSnapshotJson;
    private final String outputSchemaJson;
    private final String changeSummary;
    private final Instant registeredAt;

    public static PromptVersionResult from(PromptVersion version) {
        if (version == null) {
            return null;
        }
        return new PromptVersionResult(
                PromptVersionIdCodec.toValue(version.getId()),
                PromptTemplateIdCodec.toValue(version.getTemplateId()),
                version.getVersionNo(),
                version.getMessageTemplatesJson(),
                version.getVariablesSnapshotJson(),
                version.getOutputSchemaJson(),
                version.getChangeSummary(),
                version.getRegisteredAt());
    }
}
