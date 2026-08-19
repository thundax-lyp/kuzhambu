package com.thundax.kuzhambu.knowledge.application.graph.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchOverviewSource;
import com.thundax.kuzhambu.knowledge.application.graph.service.GraphWorkbenchRefreshReason;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewFingerprint;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchSnapshotStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GraphWorkbenchSnapshotRefresherImplTest {

    @Test
    void shouldNotRebuildWhenFingerprintAndExpiryAreUnchanged() {
        GraphWorkbenchOverviewSource source = mock(GraphWorkbenchOverviewSource.class);
        GraphWorkbenchSnapshotStore store = mock(GraphWorkbenchSnapshotStore.class);
        GraphWorkbenchOverviewFingerprint fingerprint = fingerprint(120_000L);
        when(source.getFingerprint()).thenReturn(fingerprint);
        when(store.get()).thenReturn(Optional.of(snapshot(fingerprint.value())));

        refresher(source, store).refreshIfRequired(GraphWorkbenchRefreshReason.FINGERPRINT_CHANGED);

        verify(store, never()).getByLock();
        verify(source, never()).load();
    }

    @Test
    void shouldRebuildMissingSnapshotOnceUnderReturnedLockToken() {
        GraphWorkbenchOverviewSource source = mock(GraphWorkbenchOverviewSource.class);
        GraphWorkbenchSnapshotStore store = mock(GraphWorkbenchSnapshotStore.class);
        GraphWorkbenchOverviewFingerprint fingerprint = fingerprint(null);
        GraphWorkbenchOverviewSnapshot rebuilt = snapshot(fingerprint.value());
        when(source.getFingerprint()).thenReturn(fingerprint);
        when(store.get()).thenReturn(Optional.empty());
        when(store.getByLock()).thenReturn(Optional.of("lock-token"));
        when(source.load()).thenReturn(rebuilt);

        refresher(source, store).refreshIfRequired(GraphWorkbenchRefreshReason.CACHE_MISSING);

        verify(store).replace(rebuilt);
        verify(store).deleteByLockToken("lock-token");
    }

    @Test
    void shouldKeepExistingSnapshotWhenRebuildFails() {
        GraphWorkbenchOverviewSource source = mock(GraphWorkbenchOverviewSource.class);
        GraphWorkbenchSnapshotStore store = mock(GraphWorkbenchSnapshotStore.class);
        GraphWorkbenchOverviewFingerprint before = fingerprint(null);
        GraphWorkbenchOverviewFingerprint changed = fingerprint(120_000L);
        when(source.getFingerprint()).thenReturn(changed);
        when(store.get()).thenReturn(Optional.of(snapshot(before.value())));
        when(store.getByLock()).thenReturn(Optional.of("lock-token"));
        when(source.load()).thenThrow(new IllegalStateException("database unavailable"));

        refresher(source, store).refreshIfRequired(GraphWorkbenchRefreshReason.FINGERPRINT_CHANGED);

        verify(store, never()).replace(org.mockito.ArgumentMatchers.any());
        verify(store).deleteByLockToken("lock-token");
    }

    @Test
    void shouldRefreshWhenPendingConflictExpiryIsDue() {
        GraphWorkbenchOverviewSource source = mock(GraphWorkbenchOverviewSource.class);
        GraphWorkbenchSnapshotStore store = mock(GraphWorkbenchSnapshotStore.class);
        GraphWorkbenchOverviewFingerprint fingerprint = fingerprint(1_000L);
        GraphWorkbenchOverviewSnapshot rebuilt = snapshot(fingerprint.value());
        when(source.getFingerprint()).thenReturn(fingerprint);
        when(store.get()).thenReturn(Optional.of(snapshot(fingerprint.value())));
        when(store.getByLock()).thenReturn(Optional.of("lock-token"));
        when(source.load()).thenReturn(rebuilt);

        refresher(source, store).refreshIfRequired(GraphWorkbenchRefreshReason.TOKEN_EXPIRING);

        verify(store).replace(rebuilt);
    }

    private static GraphWorkbenchSnapshotRefresherImpl refresher(
            GraphWorkbenchOverviewSource source, GraphWorkbenchSnapshotStore store) {
        return new GraphWorkbenchSnapshotRefresherImpl(source, store)
                .useClock(java.time.Clock.fixed(Instant.ofEpochMilli(1_000L), java.time.ZoneOffset.UTC));
    }

    private static GraphWorkbenchOverviewFingerprint fingerprint(Long nextExpiresAt) {
        return new GraphWorkbenchOverviewFingerprint(
                1L, 10L, 2L, 20L, 3L, 30L, 4L, 40L, 5L, 50L, 6L, 60L, 7L, 70L, 8L, nextExpiresAt, "schema-v1");
    }

    private static GraphWorkbenchOverviewSnapshot snapshot(String fingerprint) {
        return new GraphWorkbenchOverviewSnapshot(
                Instant.ofEpochMilli(500L), fingerprint, 1L, 2L, 3L, 4L, 5L, 6L, List.of());
    }
}
