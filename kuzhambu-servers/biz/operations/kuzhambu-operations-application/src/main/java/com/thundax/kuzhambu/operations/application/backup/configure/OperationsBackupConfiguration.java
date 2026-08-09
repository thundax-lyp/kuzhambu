package com.thundax.kuzhambu.operations.application.backup.configure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    OperationsBackupScheduleProperties.class,
    OperationsBackupScriptProperties.class
})
public class OperationsBackupConfiguration {}
