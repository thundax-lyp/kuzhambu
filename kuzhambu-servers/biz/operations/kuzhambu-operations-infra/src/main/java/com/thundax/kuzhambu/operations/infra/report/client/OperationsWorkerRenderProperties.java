package com.thundax.kuzhambu.operations.infra.report.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class OperationsWorkerRenderProperties {

    @Value("${kuzhambu.operations.worker.base-url:http://localhost:8000}")
    private String baseUrl;

    @Value("${kuzhambu.operations.worker.internal-secret:}")
    private String internalSecret;

    @Value("${kuzhambu.operations.worker.service-name:kuzhambu-operations}")
    private String serviceName;

    @Value("${kuzhambu.operations.worker.timeout-ms:60000}")
    private long timeoutMs;
}
