package com.thundax.kuzhambu.operations.application.health.configure;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.health.probes")
public class OperationsExternalHealthProbeProperties {

    private boolean enabled = false;
    private int timeoutMs = 3000;
    private List<Target> targets = new ArrayList<>();

    @Getter
    @Setter
    public static class Target {
        private boolean enabled = true;
        private String component;
        private String url;
        private int expectedStatus = 200;
        private int degradedLatencyMs = 1000;
    }
}
