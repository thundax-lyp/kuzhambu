package com.thundax.kuzhambu.common.knowledge.configure;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.knowledge")
public class KuzhambuKnowledgeProperties {

    private boolean enabled;
    private String provider = "fastgpt";
    private FastGpt fastgpt = new FastGpt();

    @Getter
    @Setter
    public static class FastGpt {

        private String baseUrl = "http://localhost:13000";
        private String apiKey;
        private String appId;
        private Duration timeout = Duration.ofSeconds(10);
    }
}
