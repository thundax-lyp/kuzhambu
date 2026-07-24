package com.thundax.kuzhambu.ai.application.invocation.command;

import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCallRecord;
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

    public AiCallRecord toRunningCallRecord() {
        AiCallRecord record = new AiCallRecord();
        record.setBatchId(batchId);
        record.setScope(scope);
        record.setCapability(capability);
        record.setContentType(contentType);
        record.setContentId(contentId);
        record.setObjectId(objectId);
        record.setServiceId(serviceId);
        record.setServiceRole(serviceRole);
        record.setModelId(modelId);
        record.setModelName(modelName);
        record.setPromptVersionId(promptVersionId);
        record.setRequestId(requestId);
        record.setTraceId(traceId);
        record.setStreamUsed(stream);
        record.setRequestedAt(Instant.now());
        return record;
    }
}
