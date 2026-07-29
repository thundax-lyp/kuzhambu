package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public class GetCurrentPromptVersionQuery {

    private final PromptTemplateId templateId;

    public GetCurrentPromptVersionQuery(PromptTemplateId templateId) {
        this.templateId = templateId;
    }

    public PromptTemplateId getTemplateId() {
        return templateId;
    }
}
