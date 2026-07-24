package com.thundax.kuzhambu.ai.application.platform.command;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.platform.support.PlatformAiWorkerUsecaseSpec;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformAiInvokeCommand {

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
    private boolean forceJson;
    private String locale;
    private boolean allowFallback;
    private Boolean createCandidate;

    public AiInvokeCommand toInvokeCommand(PlatformAiWorkerUsecaseSpec spec) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("platform");
        command.setCapability(spec.capability());
        command.setWorkerCapability(spec.workerCapability());
        command.setOperation(spec.operation());
        command.setWorkerPath(spec.workerPath());
        command.setContentType(contentType);
        command.setContentId(contentId);
        command.setObjectId(objectId);
        command.setServiceId(serviceId);
        command.setServiceRole(serviceRole);
        command.setModelId(modelId);
        command.setModelName(modelName);
        command.setPromptVersionId(promptVersionId);
        command.setRequestId(requestId);
        command.setTraceId(traceId);
        command.setPromptMessagesJson(promptMessagesJson);
        command.setPromptVariablesJson(promptVariablesJson);
        command.setPromptHash(promptHash);
        command.setInputPayloadJson(inputPayloadJson);
        command.setOutputSchemaJson(outputSchemaJson);
        command.setStream(false);
        command.setForceJson(forceJson);
        command.setLocale(locale);
        command.setAllowFallback(allowFallback);
        command.setCreateCandidate(createCandidate == null ? spec.defaultCreateCandidate() : createCandidate);
        return command;
    }
}
