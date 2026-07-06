package com.thundax.kuzhambu.operations.application.backup.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class OperationsBackupScheduleProperties {

    @Value("${kuzhambu.operations.backup.schedule.enabled:true}")
    private boolean enabled;

    @Value("${kuzhambu.operations.backup.schedule.startup-enabled:true}")
    private boolean startupEnabled;

    @Value("${kuzhambu.operations.backup.schedule.daily-cron:0 0 2 * * ?}")
    private String dailyCron;
}
