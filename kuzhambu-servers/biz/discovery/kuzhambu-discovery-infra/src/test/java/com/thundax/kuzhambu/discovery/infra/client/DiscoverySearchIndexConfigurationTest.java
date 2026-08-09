package com.thundax.kuzhambu.discovery.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.discovery.infra.configure.DiscoverySearchIndexConfiguration;
import com.thundax.kuzhambu.discovery.infra.configure.DiscoverySearchIndexProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DiscoverySearchIndexConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DiscoverySearchIndexConfiguration.class));

    @Test
    void shouldBindSearchIndexProperties() {
        contextRunner
                .withPropertyValues(
                        "kuzhambu.discovery.search.index.index-name=discovery-smoke",
                        "kuzhambu.discovery.search.index.shard-count=2",
                        "kuzhambu.discovery.search.index.replica-count=0",
                        "kuzhambu.discovery.search.index.batch-size=50",
                        "kuzhambu.discovery.search.index.index-analyzer=ik_max_word",
                        "kuzhambu.discovery.search.index.search-analyzer=ik_smart")
                .run(context -> {
                    DiscoverySearchIndexProperties properties = context.getBean(DiscoverySearchIndexProperties.class);

                    assertEquals("discovery-smoke", properties.getIndexName());
                    assertEquals(2, properties.getShardCount());
                    assertEquals(0, properties.getReplicaCount());
                    assertEquals(50, properties.getBatchSize());
                    assertEquals("ik_max_word", properties.getIndexAnalyzer());
                    assertEquals("ik_smart", properties.getSearchAnalyzer());
                });
    }
}
