package com.thundax.kuzhambu.knowledge.infra.graph.workbench;

import com.thundax.kuzhambu.common.cache.CacheDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class GraphWorkbenchOverviewCacheDTO implements CacheDTO {
    static final int SCHEMA_VERSION = 1;

    private int schemaVersion = SCHEMA_VERSION;
    private Instant generatedAt;
    private String sourceFingerprint;
    private long publishedNodeCount;
    private long publishedEdgeCount;
    private long coveredMaterialCount;
    private long isolatedNodeCount;
    private long missingCoreRelationNodeCount;
    private long pendingConflictCount;
    private List<ActivityCacheDTO> recentActivities = new ArrayList<>();

    @Data
    public static class ActivityCacheDTO implements CacheDTO {
        private String type;
        private String contentType;
        private Long contentRefId;
        private Instant occurredAt;
        private String summary;
    }
}
