package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.batch.codec.AiBatchJobIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelNameCodec;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptVersionIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiContentRefCodec;
import com.thundax.kuzhambu.ai.domain.invocation.codec.AiTargetObjectIdCodec;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiInvocationLog;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiInvokeCommand {

    private Long batchId;
    private String scope;
    private String capability;
    private String workerCapability;
    private String operation;
    private String workerPath;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long serviceId;
    private String serviceRole;
    private Long modelId;
    private String modelName;
    private Long promptVersionId;
    private String requestId;
    private String traceId;
    private String promptMessagesJson;
    private String promptVariablesJson;
    private String promptHash;
    private String inputPayloadJson;
    private String outputSchemaJson;
    private boolean stream;
    private boolean forceJson;
    private String locale;
    private boolean allowFallback;
    private boolean createCandidate = true;

    public AiInvocationLog toRunningInvocationLog() {
        AiInvocationLog invocationLog = new AiInvocationLog();
        invocationLog.setBatchId(AiBatchJobIdCodec.toDomain(batchId));
        invocationLog.setScope(scope);
        invocationLog.setCapability(capability == null ? null : AiBusinessCapability.from(capability));
        invocationLog.setContentRef(AiContentRefCodec.toDomain(contentType, contentId));
        invocationLog.setTargetObjectId(AiTargetObjectIdCodec.toDomain(objectId));
        invocationLog.setServiceId(serviceId);
        invocationLog.setServiceRole(serviceRole);
        invocationLog.setModelId(AiModelIdCodec.toDomain(modelId));
        invocationLog.setModelName(AiModelNameCodec.toDomain(modelName));
        invocationLog.setPromptVersionId(PromptVersionIdCodec.toDomain(promptVersionId));
        invocationLog.setRequestId(requestId);
        invocationLog.setTraceId(traceId);
        invocationLog.setStreamUsed(stream);
        invocationLog.setRequestedAt(Instant.now());
        return invocationLog;
    }
}
