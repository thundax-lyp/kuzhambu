package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListPromptVersionsQuery {

    private final PromptTemplateId templateId;
}
