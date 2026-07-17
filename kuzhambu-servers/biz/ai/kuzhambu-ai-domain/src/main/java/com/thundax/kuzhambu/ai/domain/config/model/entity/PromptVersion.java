package com.thundax.kuzhambu.ai.domain.config.model.entity;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptVersion {

    private PromptVersionId id;
    private PromptTemplateId templateId;
    private int versionNo;
    private String messageTemplatesJson;
    private String variablesSnapshotJson;
    private String outputSchemaJson;
    private String changeSummary;
    private Instant registeredAt;
}
