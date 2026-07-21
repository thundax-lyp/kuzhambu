package com.thundax.kuzhambu.ai.application.discovery.service;

import com.thundax.kuzhambu.ai.application.invocation.result.AiStreamEventResult;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;
import java.util.function.Consumer;

public interface DiscoveryAiApplicationService {

    DiscoveryAiResult understandQuery(DiscoveryAiRequest request);

    DiscoveryAiResult rewriteQuery(DiscoveryAiRequest request);

    DiscoveryAiResult generateAnswer(DiscoveryAiRequest request);

    DiscoveryAiResult streamAnswer(DiscoveryAiRequest request);

    DiscoveryAiResult streamAnswer(DiscoveryAiRequest request, Consumer<AiStreamEventResult> eventConsumer);
}
