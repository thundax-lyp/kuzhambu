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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiInvokeCommand {

    private AiBatchJobId batchId;
    private String scope;
    private AiBusinessCapability capability;
    private String workerCapability;
    private String operation;
    private String workerPath;
    private AiContentRef contentRef;
    private AiTargetObjectId targetObjectId;
    private Long serviceId;
    private String serviceRole;
    private AiModelId modelId;
    private AiModelName modelName;
    private PromptVersionId promptVersionId;
    private RequestId requestId;
    private TraceId traceId;
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
}
