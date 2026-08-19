package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchOverviewSource;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchRefreshReason;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchSnapshotRefresher;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchSnapshotStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GraphWorkbenchSnapshotRefresherImpl implements GraphWorkbenchSnapshotRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphWorkbenchSnapshotRefresherImpl.class);

    private final GraphWorkbenchOverviewSource overviewSource;
    private final GraphWorkbenchSnapshotStore snapshotStore;
    private Clock clock = Clock.systemUTC();

    public GraphWorkbenchSnapshotRefresherImpl(
            GraphWorkbenchOverviewSource overviewSource, GraphWorkbenchSnapshotStore snapshotStore) {
        this.overviewSource = overviewSource;
        this.snapshotStore = snapshotStore;
    }

    @Override
    public void refreshIfRequired(GraphWorkbenchRefreshReason reason) {
        try {
            refreshIfRequired(overviewSource.getFingerprint(), reason);
        } catch (RuntimeException exception) {
            LOGGER.warn("Graph workbench overview refresh failed, reason={}", reason, exception);
        }
    }

    GraphWorkbenchSnapshotRefresherImpl useClock(Clock clock) {
        if (clock != null) {
            this.clock = clock;
        }
        return this;
    }

    private void refreshIfRequired(GraphWorkbenchOverviewFingerprint fingerprint, GraphWorkbenchRefreshReason reason) {
        if (!requiresRefresh(snapshotStore.get(), fingerprint)) {
            return;
        }
        Optional<String> lockToken = snapshotStore.getByLock();
        if (lockToken.isEmpty()) {
            return;
        }
        try {
            GraphWorkbenchOverviewFingerprint lockedFingerprint = overviewSource.getFingerprint();
            if (!requiresRefresh(snapshotStore.get(), lockedFingerprint)) {
                return;
            }
            snapshotStore.replace(overviewSource.load());
            LOGGER.info("Graph workbench overview refreshed, reason={}", reason);
        } finally {
            snapshotStore.deleteByLockToken(lockToken.get());
        }
    }

    private boolean requiresRefresh(
            Optional<GraphWorkbenchOverviewSnapshot> currentSnapshot, GraphWorkbenchOverviewFingerprint fingerprint) {
        if (currentSnapshot.isEmpty()) {
            return true;
        }
        GraphWorkbenchOverviewSnapshot snapshot = currentSnapshot.get();
        if (!snapshot.sourceFingerprint().equals(fingerprint.value())) {
            return true;
        }
        Instant nextRefreshAt = fingerprint.nextRefreshAt();
        return nextRefreshAt != null && !nextRefreshAt.isAfter(Instant.now(clock));
    }
}
