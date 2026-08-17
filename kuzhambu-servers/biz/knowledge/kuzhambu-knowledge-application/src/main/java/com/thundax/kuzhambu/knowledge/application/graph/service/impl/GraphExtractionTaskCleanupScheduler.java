package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.ai.facade.AiFacade;
import com.thundax.kuzhambu.ai.facade.request.CleanupKnowledgeGraphCandidateFacadeRequest;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphExtractionTaskRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Lazy(false)
public class GraphExtractionTaskCleanupScheduler {
    private static final int DEFAULT_LIMIT = 100;

    private final GraphExtractionTaskRepository taskRepository;
    private final AiFacade aiFacade;
    private Clock clock = Clock.systemUTC();

    public GraphExtractionTaskCleanupScheduler(GraphExtractionTaskRepository taskRepository, AiFacade aiFacade) {
        this.taskRepository = taskRepository;
        this.aiFacade = aiFacade;
    }

    GraphExtractionTaskCleanupScheduler useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
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

    private boolean cleanupOne(GraphExtractionTask task) {
        if (task == null || task.getId() == null) {
            return false;
        }
        try {
            cleanupCandidate(task);
            return taskRepository.deleteById(task.getId()) == 1;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void cleanupCandidate(GraphExtractionTask task) {
        if (task.getCandidateId() == null) {
            return;
        }
        aiFacade.cleanupKnowledgeGraphCandidate(CleanupKnowledgeGraphCandidateFacadeRequest.builder()
                .candidateId(task.getCandidateId())
                .build());
    }
}
