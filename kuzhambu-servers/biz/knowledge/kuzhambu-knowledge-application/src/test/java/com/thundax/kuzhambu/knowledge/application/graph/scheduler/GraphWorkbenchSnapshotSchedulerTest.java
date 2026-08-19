package com.thundax.kuzhambu.knowledge.application.graph.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchRefreshReason;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchSnapshotRefresher;
import org.junit.jupiter.api.Test;

class GraphWorkbenchSnapshotSchedulerTest {

    @Test
    void shouldRequestStartupWarmupAndScheduledFingerprintCheck() {
        GraphWorkbenchSnapshotRefresher refresher = mock(GraphWorkbenchSnapshotRefresher.class);
        GraphWorkbenchSnapshotScheduler scheduler = new GraphWorkbenchSnapshotScheduler(refresher);

        scheduler.onApplicationEvent(null);
        scheduler.checkSnapshot();

        verify(refresher).refreshIfRequired(GraphWorkbenchRefreshReason.STARTUP);
        verify(refresher).refreshIfRequired(GraphWorkbenchRefreshReason.FINGERPRINT_CHANGED);
    }
}
