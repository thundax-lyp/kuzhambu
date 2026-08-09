package com.thundax.kuzhambu.operations.infra.report.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OperationsWorkerRenderProperties.class)
public class OperationsWorkerRenderConfiguration {}
