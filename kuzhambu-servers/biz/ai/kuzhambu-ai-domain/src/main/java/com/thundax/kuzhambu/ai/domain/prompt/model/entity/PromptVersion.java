package com.thundax.kuzhambu.ai.domain.prompt.model.entity;

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

    private Long id;
    private Long promptVersionId;
    private Long templateId;
    private int versionNo;
    private String messageTemplatesJson;
    private String variablesSnapshotJson;
    private String outputSchemaJson;
    private String currentKey;
    private String changeSummary;
    private Instant registeredAt;

    public boolean isCurrent() {
        return currentKey != null && !currentKey.isBlank();
    }
}
