package com.thundax.kuzhambu.operations.infra.report.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.worker")
public class OperationsWorkerRenderProperties {

    private String baseUrl = "http://localhost:8000";
    private String internalSecret = "";
    private String serviceName = "kuzhambu-operations";
    private long timeoutMs = 60000L;
}
