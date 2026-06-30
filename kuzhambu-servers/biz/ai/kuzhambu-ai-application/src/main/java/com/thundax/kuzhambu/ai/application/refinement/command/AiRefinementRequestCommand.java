package com.thundax.kuzhambu.ai.application.refinement.command;

import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRefinementRequestCommand {

    private String capability;
    private String scope;
    private String operation;
    private String contentType;
    private Long contentId;
    private Long objectId;
    private Long requestedBy;
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

    public AiInvokeCommand toInvokeCommand(String capability) {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope(scope);
        command.setCapability(capability);
        command.setOperation(operation);
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
        command.setForceJson(forceJson);
        command.setLocale(locale);
        command.setCreateCandidate(true);
        return command;
    }
}
