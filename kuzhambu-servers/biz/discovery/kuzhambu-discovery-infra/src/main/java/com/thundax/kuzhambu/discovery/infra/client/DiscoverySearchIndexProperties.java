package com.thundax.kuzhambu.discovery.infra.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscoverySearchIndexProperties {

    private String indexName = "discovery-search";

    private int shardCount = 1;

    private int replicaCount = 1;
}
