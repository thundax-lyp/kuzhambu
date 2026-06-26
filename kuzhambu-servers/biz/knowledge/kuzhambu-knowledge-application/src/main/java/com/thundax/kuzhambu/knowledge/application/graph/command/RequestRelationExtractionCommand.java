package com.thundax.kuzhambu.knowledge.application.graph.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestRelationExtractionCommand {
    private String scopeType;
    private String scopeJson;
    private String triggerSource;
    private String selectionScopeJson;
    private Boolean replaceUnconfirmedOnly;
    private Long parentTaskId;
    private String sourceContentType;
    private Long sourceContentId;
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

    public RequestRelationExtractionCommand(
            String scopeType,
            String scopeJson,
            String sourceContentType,
            Long sourceContentId,
            Long requestedBy,
            Long serviceId,
            String serviceRole,
            Long modelId,
            String modelName,
            Long promptVersionId,
            String requestId,
            String traceId,
            String promptMessagesJson,
            String promptVariablesJson,
            String promptHash,
            String inputPayloadJson,
            String outputSchemaJson,
            boolean forceJson,
            String locale) {
        this.scopeType = scopeType;
        this.scopeJson = scopeJson;
        this.sourceContentType = sourceContentType;
        this.sourceContentId = sourceContentId;
        this.requestedBy = requestedBy;
        this.serviceId = serviceId;
        this.serviceRole = serviceRole;
        this.modelId = modelId;
        this.modelName = modelName;
        this.promptVersionId = promptVersionId;
        this.requestId = requestId;
        this.traceId = traceId;
        this.promptMessagesJson = promptMessagesJson;
        this.promptVariablesJson = promptVariablesJson;
        this.promptHash = promptHash;
        this.inputPayloadJson = inputPayloadJson;
        this.outputSchemaJson = outputSchemaJson;
        this.forceJson = forceJson;
        this.locale = locale;
    }
}
