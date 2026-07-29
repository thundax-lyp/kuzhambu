package com.thundax.kuzhambu.ai.application.config.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;

public class RollbackPromptVersionCommand {

    private final PromptTemplateId templateId;
    private final int versionNo;

    public RollbackPromptVersionCommand(PromptTemplateId templateId, int versionNo) {
        this.templateId = templateId;
        this.versionNo = versionNo;
    }

    public PromptTemplateId getTemplateId() {
        return templateId;
    }

    public int getVersionNo() {
        return versionNo;
    }
}
