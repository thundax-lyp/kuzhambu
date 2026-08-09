package com.thundax.kuzhambu.operations.application.backup.configure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.operations.backup.schedule")
public class OperationsBackupScheduleProperties {

    private boolean enabled = true;
    private boolean startupEnabled = true;
    private String dailyCron = "0 0 2 * * ?";
}
