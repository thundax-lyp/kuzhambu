package com.thundax.kuzhambu.operations.application.cleanup.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OperationsCleanupScheduleProperties.class)
public class OperationsCleanupConfiguration {}
