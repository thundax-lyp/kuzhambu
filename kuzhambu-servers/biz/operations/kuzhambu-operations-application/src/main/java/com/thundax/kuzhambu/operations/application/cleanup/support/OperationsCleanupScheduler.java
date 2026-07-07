package com.thundax.kuzhambu.operations.application.cleanup.support;

import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OperationsCleanupScheduler implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsCleanupScheduler.class);

    private final CleanupApplicationService cleanupApplicationService;
    private final OperationsCleanupScheduleProperties properties;

    public OperationsCleanupScheduler(
            CleanupApplicationService cleanupApplicationService, OperationsCleanupScheduleProperties properties) {
        this.cleanupApplicationService = cleanupApplicationService;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isEnabled() || !properties.isStartupEnabled()) {
            return;
        }
        executeEnabledPolicies();
    }

    @Scheduled(cron = "${kuzhambu.operations.cleanup.schedule.daily-cron:0 30 3 * * ?}")
    public void executeDailyCleanup() {
        if (!properties.isEnabled()) {
            return;
        }
        executeEnabledPolicies();
    }

    private void executeEnabledPolicies() {
        Date requestedAt = new Date();
        for (OperationsCleanupScheduleProperties.CleanupPolicy policy : properties.orderedPolicies()) {
            if (!policy.enabled()) {
                continue;
            }
            executePolicy(policy, requestedAt);
        }
    }

    private void executePolicy(OperationsCleanupScheduleProperties.CleanupPolicy policy, Date requestedAt) {
        try {
            cleanupApplicationService.executeScheduled(new OperationsCleanupExecuteCommand(
                    policy.cleanupType(), null, requestedAt, policy.retentionDays(), policy.limit()));
        } catch (RuntimeException exception) {
            LOGGER.warn("Operations cleanup policy execution failed, cleanupType={}", policy.cleanupType(), exception);
        }
    }
}
