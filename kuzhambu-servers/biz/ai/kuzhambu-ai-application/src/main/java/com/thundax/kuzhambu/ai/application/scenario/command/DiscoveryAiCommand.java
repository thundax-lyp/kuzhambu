package com.thundax.kuzhambu.ai.application.scenario.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;

public record DiscoveryAiCommand(
        Long serviceId,
        String serviceRole,
        AiModelId modelId,
        AiModelName modelName,
        PromptVersionId promptVersionId,
        RequestId requestId,
        TraceId traceId,
        String promptMessagesJson,
        String promptVariablesJson,
        String promptHash,
        String inputPayloadJson,
        String outputSchemaJson,
        boolean stream,
        boolean forceJson,
        String locale) {}
