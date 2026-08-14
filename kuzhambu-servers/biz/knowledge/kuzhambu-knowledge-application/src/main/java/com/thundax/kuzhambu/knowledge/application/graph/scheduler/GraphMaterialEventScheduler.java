package com.thundax.kuzhambu.knowledge.application.graph.scheduler;

import com.thundax.kuzhambu.knowledge.application.graph.service.GraphMaterialEventApplicationService;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphMaterialEventRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GraphMaterialEventScheduler {

    private static final int BATCH_SIZE = 20;
    private static final Duration PROCESSING_LEASE_TIMEOUT = Duration.ofMinutes(5L);
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphMaterialEventScheduler.class);

    private final GraphMaterialEventRepository eventRepository;
    private final GraphMaterialEventApplicationService eventApplicationService;

    public GraphMaterialEventScheduler(
            GraphMaterialEventRepository eventRepository,
            GraphMaterialEventApplicationService eventApplicationService) {
        this.eventRepository = eventRepository;
        this.eventApplicationService = eventApplicationService;
    }

    @Scheduled(fixedDelayString = "${kuzhambu.knowledge.graph.material-event-fixed-delay:30s}")
    public void processScheduledEvents() {
        reclaimStaleProcessingEvents();
        for (GraphMaterialEvent event : eventRepository.listByStatus(GraphMaterialEventStatus.SCHEDULED, BATCH_SIZE)) {
            try {
                eventApplicationService.processScheduledEvent(event.getId());
            } catch (RuntimeException ex) {
                LOGGER.warn("Graph material event processing failed, eventId={}", event.getId(), ex);
            }
        }
    }

    private void reclaimStaleProcessingEvents() {
        Instant changedBefore = Instant.now().minus(PROCESSING_LEASE_TIMEOUT);
        for (GraphMaterialEvent event : eventRepository.listProcessingBefore(changedBefore, BATCH_SIZE)) {
            try {
                eventApplicationService.reclaimStaleProcessingEvent(event.getId());
            } catch (RuntimeException ex) {
                LOGGER.warn("Graph material event reclaim failed, eventId={}", event.getId(), ex);
            }
        }
    }
}
