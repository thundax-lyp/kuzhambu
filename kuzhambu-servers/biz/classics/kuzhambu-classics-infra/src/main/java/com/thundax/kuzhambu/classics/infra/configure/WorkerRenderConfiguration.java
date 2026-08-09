package com.thundax.kuzhambu.classics.infra.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WorkerRenderProperties.class)
public class WorkerRenderConfiguration {}
