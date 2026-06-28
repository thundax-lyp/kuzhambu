package com.thundax.kuzhambu.ai.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscoveryAiFacadeRequest {

    private final Long serviceId;
    private final String serviceRole;
    private final Long modelId;
    private final String modelName;
    private final Long promptVersionId;
    private final String requestId;
    private final String traceId;
    private final String promptMessagesJson;
    private final String promptVariablesJson;
    private final String promptHash;
    private final String inputPayloadJson;
    private final String outputSchemaJson;
    private final boolean stream;
    private final boolean forceJson;
    private final String locale;
}
