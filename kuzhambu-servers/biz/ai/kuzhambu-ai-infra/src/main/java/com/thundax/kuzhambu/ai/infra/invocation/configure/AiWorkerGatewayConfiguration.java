package com.thundax.kuzhambu.ai.infra.invocation.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiWorkerGatewayProperties.class)
public class AiWorkerGatewayConfiguration {}
