package com.thundax.kuzhambu.ai.application.scenario.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;

public record KnowledgeAiExtractionCommand(
        AiBatchJobId batchId,
        String taskType,
        String scopeType,
        String scopeJson,
        String sourceContentType,
        Long sourceContentId,
        Long requestedBy,
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
        boolean forceJson,
        String locale) {}
