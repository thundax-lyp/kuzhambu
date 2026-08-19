package com.thundax.kuzhambu.knowledge.infra.graph.workbench;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchActivity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class RedisGraphWorkbenchSnapshotStoreTest {

    @Test
    void snapshotShouldRoundTripOnlyThroughRemoteCache() throws Exception {
        Cache<String, GraphWorkbenchOverviewCacheDTO> overviewCache = mock(Cache.class);
        RedisGraphWorkbenchSnapshotStore store = store(overviewCache, mock(Cache.class));
        GraphWorkbenchOverviewSnapshot snapshot = snapshot();

        store.replace(snapshot);

        ArgumentCaptor<GraphWorkbenchOverviewCacheDTO> captor =
                ArgumentCaptor.forClass(GraphWorkbenchOverviewCacheDTO.class);
        verify(overviewCache).put(eq(RedisGraphWorkbenchSnapshotStore.OVERVIEW_KEY), captor.capture());
        when(overviewCache.get(RedisGraphWorkbenchSnapshotStore.OVERVIEW_KEY)).thenReturn(captor.getValue());
        assertThat(store.get()).contains(snapshot);

        assertThat(cacheTypeOf("overviewCache")).isEqualTo(CacheType.REMOTE);
        assertThat(cacheTypeOf("lockCache")).isEqualTo(CacheType.REMOTE);
    }

    @Test
    void unknownCacheSchemaShouldNotBeReturned() {
        Cache<String, GraphWorkbenchOverviewCacheDTO> overviewCache = mock(Cache.class);
        RedisGraphWorkbenchSnapshotStore store = store(overviewCache, mock(Cache.class));
        GraphWorkbenchOverviewCacheDTO cacheDTO = new GraphWorkbenchOverviewCacheDTO();
        cacheDTO.setSchemaVersion(2);
        when(overviewCache.get(RedisGraphWorkbenchSnapshotStore.OVERVIEW_KEY)).thenReturn(cacheDTO);

        assertThat(store.get()).isEmpty();
    }

    @Test
    void onlyTheReturnedTokenMayReleaseItsRemoteLock() {
        Cache<String, Object> lockCache = mock(Cache.class);
        AutoReleaseLock lock = mock(AutoReleaseLock.class);
        when(lockCache.tryLock(eq(RedisGraphWorkbenchSnapshotStore.LOCK_KEY), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(lock);
        RedisGraphWorkbenchSnapshotStore store = store(mock(Cache.class), lockCache);

        String token = store.getByLock().orElseThrow();
        store.deleteByLockToken("different-token");
        verify(lock, never()).close();

        store.deleteByLockToken(token);
        verify(lock).close();
    }

    private static RedisGraphWorkbenchSnapshotStore store(
            Cache<String, GraphWorkbenchOverviewCacheDTO> overviewCache, Cache<String, Object> lockCache) {
        RedisGraphWorkbenchSnapshotStore store = new RedisGraphWorkbenchSnapshotStore();
        ReflectionTestUtils.setField(store, "overviewCache", overviewCache);
        ReflectionTestUtils.setField(store, "lockCache", lockCache);
        return store;
    }

    private static CacheType cacheTypeOf(String fieldName) throws NoSuchFieldException {
        Field field = RedisGraphWorkbenchSnapshotStore.class.getDeclaredField(fieldName);
        return field.getAnnotation(CreateCache.class).cacheType();
    }

    private static GraphWorkbenchOverviewSnapshot snapshot() {
        return new GraphWorkbenchOverviewSnapshot(
                Instant.parse("2026-08-19T03:00:00Z"),
                "source-v1",
                12L,
                18L,
                4L,
                1L,
                2L,
                3L,
                List.of(new GraphWorkbenchActivity(
                        "PUBLICATION",
                        new ContentRef("SANCAI_ENTRY", 1001L),
                        Instant.parse("2026-08-19T02:00:00Z"),
                        "发布素材")));
    }
}
