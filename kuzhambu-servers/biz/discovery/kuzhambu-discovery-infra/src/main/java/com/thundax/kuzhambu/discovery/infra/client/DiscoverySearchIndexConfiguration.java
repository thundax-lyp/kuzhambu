package com.thundax.kuzhambu.discovery.infra.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DiscoverySearchIndexProperties.class)
public class DiscoverySearchIndexConfiguration {}
