package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;

public record AiInvokePrompt(
        PromptVersionId promptVersionId, String promptMessagesJson, String promptVariablesJson, String promptHash) {}
