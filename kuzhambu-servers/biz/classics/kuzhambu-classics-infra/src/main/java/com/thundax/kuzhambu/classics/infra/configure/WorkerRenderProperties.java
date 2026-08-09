package com.thundax.kuzhambu.classics.infra.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.classics.worker")
public class WorkerRenderProperties {

    private String baseUrl = "http://localhost:8000";
    private String internalSecret = "";
    private String serviceName = "kuzhambu-classics";
    private long timeoutMs = 60000L;
}
