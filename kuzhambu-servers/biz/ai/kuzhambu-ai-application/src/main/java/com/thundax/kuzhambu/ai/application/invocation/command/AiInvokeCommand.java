package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiBatchJobId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;

public record AiInvokeCommand(
        AiBatchJobId batchId,
        String scope,
        AiBusinessCapability capability,
        String workerCapability,
        String operation,
        String workerPath,
        AiContentRef contentRef,
        AiTargetObjectId targetObjectId,
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
        String locale,
        boolean allowFallback,
        boolean createCandidate) {}
