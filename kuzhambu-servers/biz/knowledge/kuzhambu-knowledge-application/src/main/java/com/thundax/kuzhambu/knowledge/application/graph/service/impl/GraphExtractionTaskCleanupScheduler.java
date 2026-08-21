package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.knowledge.application.graph.operator.GraphExtractionTaskCleanupOperator;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphExtractionApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class GraphExtractionTaskCleanupScheduler implements ApplicationListener<ApplicationReadyEvent> {
    private static final int DEFAULT_LIMIT = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphExtractionTaskCleanupScheduler.class);

    private final GraphExtractionTaskRepository taskRepository;
    private final GraphExtractionTaskCleanupOperator cleanupOperator;
    private final GraphExtractionApplicationService extractionService;
    private Clock clock = Clock.systemUTC();

    public GraphExtractionTaskCleanupScheduler(
            GraphExtractionTaskRepository taskRepository,
            GraphExtractionTaskCleanupOperator cleanupOperator,
            GraphExtractionApplicationService extractionService) {
        this.taskRepository = taskRepository;
        this.cleanupOperator = cleanupOperator;
        this.extractionService = extractionService;
    }

    GraphExtractionTaskCleanupScheduler useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            extractionService.recoverActiveTasksAtStartup();
        } catch (RuntimeException ex) {
            LOGGER.warn("Graph extraction startup task synchronization failed", ex);
        }
    }

    @Scheduled(cron = "0 20 2 * * ?")
    public int cleanupExpiredTasks() {
        List<GraphExtractionTask> candidates = taskRepository.listPurgeableBefore(Instant.now(clock), DEFAULT_LIMIT);
        int count = 0;
        for (GraphExtractionTask task : candidates) {
            if (cleanupOne(task)) {
                count++;
            }
        }
        return count;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.knowledge.graph.task-sync-delay-ms:60000}")
    public int syncActiveTasks() {
        return extractionService.syncActiveTasks();
    }

    private boolean cleanupOne(GraphExtractionTask task) {
        if (task == null || task.getId() == null) {
            return false;
        }
        try {
            cleanupOperator.cleanup(task);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
