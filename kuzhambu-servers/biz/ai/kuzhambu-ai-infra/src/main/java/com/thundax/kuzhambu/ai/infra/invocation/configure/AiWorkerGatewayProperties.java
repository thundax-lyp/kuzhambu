package com.thundax.kuzhambu.ai.infra.invocation.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.ai.worker")
public class AiWorkerGatewayProperties {

    private String baseUrl = "http://localhost:8000";
    private String internalSecret = "";
    private String serviceName = "kuzhambu-ai";
    private long timeoutMs = 300000L;
    private long streamIdleTimeoutMs = 30000L;
    private long maxArtifactSizeBytes = 52428800L;
}
