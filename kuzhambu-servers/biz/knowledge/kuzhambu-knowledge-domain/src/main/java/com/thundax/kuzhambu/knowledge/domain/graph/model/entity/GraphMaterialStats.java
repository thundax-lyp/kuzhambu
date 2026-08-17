package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialStats {
    private Long materialId;
    private long draftNodeCount;
    private long draftEdgeCount;
    private long publishedNodeCount;
    private long publishedEdgeCount;
    private long activeTaskCount;
    private long pendingReviewTaskCount;
    private long failedTaskCount;
    private long statsRevision;
    private Instant calculatedAt;
}
