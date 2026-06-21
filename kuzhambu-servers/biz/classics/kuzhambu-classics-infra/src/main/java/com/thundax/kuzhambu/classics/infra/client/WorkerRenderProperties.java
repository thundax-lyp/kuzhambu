package com.thundax.kuzhambu.classics.infra.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class WorkerRenderProperties {

    @Value("${kuzhambu.classics.worker.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${kuzhambu.classics.worker.internal-secret:}")
    private String internalSecret;

    @Value("${kuzhambu.classics.worker.service-name:kuzhambu-classics}")
    private String serviceName;

    @Value("${kuzhambu.classics.worker.timeout-ms:60000}")
    private long timeoutMs;
}
