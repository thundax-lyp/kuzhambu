package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublishedEdgeMaterial {
    private GraphPublishedEdgeId publishedEdgeId;
    private ContentRef materialRef;
    private String sourceSnapshotJson;
    private long changedAt;

    public GraphPublishedEdgeMaterial(
            GraphPublishedEdgeId publishedEdgeId, ContentRef materialRef, String sourceSnapshotJson) {
        this(publishedEdgeId, materialRef, sourceSnapshotJson, 0L);
    }
}
