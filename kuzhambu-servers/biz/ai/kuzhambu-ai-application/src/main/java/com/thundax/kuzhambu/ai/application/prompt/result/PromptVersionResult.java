package com.thundax.kuzhambu.ai.application.prompt.result;

import com.thundax.kuzhambu.ai.domain.prompt.model.entity.PromptVersion;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PromptVersionResult {

    private final Long promptVersionId;
    private final Long templateId;
    private final int versionNo;
    private final String messageTemplatesJson;
    private final String variablesSnapshotJson;
    private final String outputSchemaJson;
    private final boolean current;
    private final String changeSummary;
    private final Instant registeredAt;

    public static PromptVersionResult from(PromptVersion version) {
        if (version == null) {
            return null;
        }
        return new PromptVersionResult(
                version.getPromptVersionId(),
                version.getTemplateId(),
                version.getVersionNo(),
                version.getMessageTemplatesJson(),
                version.getVariablesSnapshotJson(),
                version.getOutputSchemaJson(),
                version.isCurrent(),
                version.getChangeSummary(),
                version.getRegisteredAt());
    }
}
