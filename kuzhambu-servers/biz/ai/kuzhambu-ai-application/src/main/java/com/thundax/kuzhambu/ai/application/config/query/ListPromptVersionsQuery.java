package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public class ListPromptVersionsQuery {

    private final PromptTemplateId templateId;

    public ListPromptVersionsQuery(PromptTemplateId templateId) {
        this.templateId = templateId;
    }

    public PromptTemplateId getTemplateId() {
        return templateId;
    }
}
