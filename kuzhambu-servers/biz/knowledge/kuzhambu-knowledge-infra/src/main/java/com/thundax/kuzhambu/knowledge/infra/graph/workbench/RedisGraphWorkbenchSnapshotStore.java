package com.thundax.kuzhambu.knowledge.infra.graph.workbench;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.thundax.kuzhambu.common.cache.KuzhambuCacheNames;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchActivity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.readmodel.GraphWorkbenchOverviewSnapshot;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphWorkbenchSnapshotStore;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class RedisGraphWorkbenchSnapshotStore implements GraphWorkbenchSnapshotStore {

    static final String OVERVIEW_CACHE_NAME = KuzhambuCacheNames.PREFIX + "knowledge.graph.workbench.overview.v1";
    static final String LOCK_CACHE_NAME = KuzhambuCacheNames.PREFIX + "knowledge.graph.workbench.refresh-lock.v1";
    static final String OVERVIEW_KEY = "overview";
    static final String LOCK_KEY = "refresh";
    static final long LOCK_LEASE_SECONDS = 30L;

    @CreateCache(name = OVERVIEW_CACHE_NAME, cacheType = CacheType.REMOTE)
    private Cache<String, GraphWorkbenchOverviewCacheDTO> overviewCache;

    @CreateCache(name = LOCK_CACHE_NAME, cacheType = CacheType.REMOTE)
    private Cache<String, Object> lockCache;

    private final Map<String, AutoReleaseLock> locksByToken = new ConcurrentHashMap<>();

    @Override
    public Optional<GraphWorkbenchOverviewSnapshot> get() {
        return Optional.ofNullable(overviewCache.get(OVERVIEW_KEY))
                .filter(cacheDTO -> cacheDTO.getSchemaVersion() == GraphWorkbenchOverviewCacheDTO.SCHEMA_VERSION)
                .map(this::toSnapshot);
    }

    @Override
    public void replace(GraphWorkbenchOverviewSnapshot snapshot) {
        if (snapshot != null) {
            overviewCache.put(OVERVIEW_KEY, toCacheDTO(snapshot));
        }
    }

    @Override
    public Optional<String> getByLock() {
        AutoReleaseLock lock = lockCache.tryLock(LOCK_KEY, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
        if (lock == null) {
            return Optional.empty();
        }
        String token = UUID.randomUUID().toString();
        locksByToken.put(token, lock);
        return Optional.of(token);
    }

    @Override
    public void deleteByLockToken(String token) {
        AutoReleaseLock lock = token == null ? null : locksByToken.remove(token);
        if (lock != null) {
            lock.close();
        }
    }

    private GraphWorkbenchOverviewSnapshot toSnapshot(GraphWorkbenchOverviewCacheDTO cacheDTO) {
        return new GraphWorkbenchOverviewSnapshot(
                cacheDTO.getGeneratedAt(),
                cacheDTO.getSourceFingerprint(),
                cacheDTO.getPublishedNodeCount(),
                cacheDTO.getPublishedEdgeCount(),
                cacheDTO.getCoveredMaterialCount(),
                cacheDTO.getIsolatedNodeCount(),
                cacheDTO.getMissingCoreRelationNodeCount(),
                cacheDTO.getPendingConflictCount(),
                cacheDTO.getRecentActivities().stream().map(this::toActivity).toList());
    }

    private GraphWorkbenchOverviewCacheDTO toCacheDTO(GraphWorkbenchOverviewSnapshot snapshot) {
        GraphWorkbenchOverviewCacheDTO cacheDTO = new GraphWorkbenchOverviewCacheDTO();
        cacheDTO.setGeneratedAt(snapshot.generatedAt());
        cacheDTO.setSourceFingerprint(snapshot.sourceFingerprint());
        cacheDTO.setPublishedNodeCount(snapshot.publishedNodeCount());
        cacheDTO.setPublishedEdgeCount(snapshot.publishedEdgeCount());
        cacheDTO.setCoveredMaterialCount(snapshot.coveredMaterialCount());
        cacheDTO.setIsolatedNodeCount(snapshot.isolatedNodeCount());
        cacheDTO.setMissingCoreRelationNodeCount(snapshot.missingCoreRelationNodeCount());
        cacheDTO.setPendingConflictCount(snapshot.pendingConflictCount());
        cacheDTO.setRecentActivities(
                snapshot.recentActivities().stream().map(this::toCacheActivity).toList());
        return cacheDTO;
    }

    private GraphWorkbenchActivity toActivity(GraphWorkbenchOverviewCacheDTO.ActivityCacheDTO cacheDTO) {
        ContentRef contentRef = cacheDTO.getContentType() == null || cacheDTO.getContentRefId() == null
                ? null
                : new ContentRef(cacheDTO.getContentType(), cacheDTO.getContentRefId());
        return new GraphWorkbenchActivity(
                cacheDTO.getType(), contentRef, cacheDTO.getOccurredAt(), cacheDTO.getSummary());
    }

    private GraphWorkbenchOverviewCacheDTO.ActivityCacheDTO toCacheActivity(GraphWorkbenchActivity activity) {
        GraphWorkbenchOverviewCacheDTO.ActivityCacheDTO cacheDTO =
                new GraphWorkbenchOverviewCacheDTO.ActivityCacheDTO();
        cacheDTO.setType(activity.type());
        cacheDTO.setContentType(
                activity.contentRef() == null ? null : activity.contentRef().getContentType());
        cacheDTO.setContentRefId(
                activity.contentRef() == null ? null : activity.contentRef().getContentId());
        cacheDTO.setOccurredAt(activity.occurredAt());
        cacheDTO.setSummary(activity.summary());
        return cacheDTO;
    }
}
