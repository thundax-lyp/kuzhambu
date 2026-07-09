package com.thundax.kuzhambu.discovery.infra.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.discovery.search.index")
public class DiscoverySearchIndexProperties {

    private String indexName = "discovery-search";

    private int shardCount = 1;

    private int replicaCount = 1;

    private int batchSize = 200;
}
