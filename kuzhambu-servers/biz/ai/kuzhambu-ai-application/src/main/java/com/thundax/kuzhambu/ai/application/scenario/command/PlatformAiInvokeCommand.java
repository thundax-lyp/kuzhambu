package com.thundax.kuzhambu.ai.application.scenario.command;

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
}
