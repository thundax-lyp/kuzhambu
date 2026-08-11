package com.thundax.kuzhambu.operations.application.cleanup.support;

import com.thundax.kuzhambu.operations.application.cleanup.configure.OperationsCleanupScheduleProperties;
import com.thundax.kuzhambu.operations.application.cleanup.facade.assembler.OperationsCleanupSchedulerFacadeAssembler;
import com.thundax.kuzhambu.operations.application.cleanup.service.CleanupApplicationService;
import java.time.Instant;
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
        Instant requestedAt = Instant.now();
        for (OperationsCleanupPolicies.CleanupPolicy policy : OperationsCleanupPolicies.orderedPolicies(properties)) {
            if (!policy.enabled()) {
                continue;
            }
            executePolicy(policy, requestedAt);
        }
    }

    private void executePolicy(OperationsCleanupPolicies.CleanupPolicy policy, Instant requestedAt) {
        try {
            cleanupApplicationService.executeScheduled(
                    OperationsCleanupSchedulerFacadeAssembler.toCommand(policy, requestedAt));
        } catch (RuntimeException exception) {
            LOGGER.warn("Operations cleanup policy execution failed, cleanupType={}", policy.cleanupType(), exception);
        }
    }
}
