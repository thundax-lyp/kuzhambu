package com.thundax.kuzhambu.ai.application.scenario.command;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef;
import com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiTargetObjectId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformAiInvokeCommand {

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
    private boolean forceJson;
    private String locale;
    private boolean allowFallback;
    private Boolean createCandidate;
}
