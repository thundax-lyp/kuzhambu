package com.thundax.kuzhambu.knowledge.application.graph.scheduler;

import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchRefreshReason;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchSnapshotRefresher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GraphWorkbenchSnapshotScheduler implements ApplicationListener<ApplicationReadyEvent> {

    private final GraphWorkbenchSnapshotRefresher refresher;

    public GraphWorkbenchSnapshotScheduler(GraphWorkbenchSnapshotRefresher refresher) {
        this.refresher = refresher;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        refresher.refreshIfRequired(GraphWorkbenchRefreshReason.STARTUP);
    }

    @Scheduled(fixedDelay = 30_000L)
    public void checkSnapshot() {
        refresher.refreshIfRequired(GraphWorkbenchRefreshReason.FINGERPRINT_CHANGED);
    }
}
