package com.thundax.kuzhambu.ai.infra.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class WorkerAiProperties {

    @Value("${kuzhambu.ai.worker.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${kuzhambu.ai.worker.internal-secret:}")
    private String internalSecret;

    @Value("${kuzhambu.ai.worker.service-name:kuzhambu-ai}")
    private String serviceName;

    @Value("${kuzhambu.ai.worker.timeout-ms:60000}")
    private long timeoutMs;

    @Value("${kuzhambu.ai.worker.max-artifact-size-bytes:52428800}")
    private long maxArtifactSizeBytes;
}
