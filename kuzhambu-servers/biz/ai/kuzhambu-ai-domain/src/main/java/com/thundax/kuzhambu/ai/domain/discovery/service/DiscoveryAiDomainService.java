package com.thundax.kuzhambu.ai.domain.discovery.service;

import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiRequest;
import com.thundax.kuzhambu.ai.domain.discovery.model.valueobject.DiscoveryAiResult;

public interface DiscoveryAiDomainService {

    DiscoveryAiResult understandQuery(DiscoveryAiRequest request);

    DiscoveryAiResult rewriteQuery(DiscoveryAiRequest request);

    DiscoveryAiResult generateAnswer(DiscoveryAiRequest request);

    DiscoveryAiResult streamAnswer(DiscoveryAiRequest request);
}
