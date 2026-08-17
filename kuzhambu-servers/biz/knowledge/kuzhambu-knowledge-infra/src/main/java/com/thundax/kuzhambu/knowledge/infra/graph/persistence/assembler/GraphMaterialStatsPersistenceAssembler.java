package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialStats;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialStatsDO;

public final class GraphMaterialStatsPersistenceAssembler {
    private GraphMaterialStatsPersistenceAssembler() {}

    public static GraphMaterialStatsDO toObject(GraphMaterialStats entity) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialStatsDO(
                entity.getMaterialId(),
                entity.getDraftNodeCount(),
                entity.getDraftEdgeCount(),
                entity.getPublishedNodeCount(),
                entity.getPublishedEdgeCount(),
                entity.getActiveTaskCount(),
                entity.getPendingReviewTaskCount(),
                entity.getFailedTaskCount(),
                entity.getStatsRevision(),
                entity.getCalculatedAt());
    }

    public static GraphMaterialStats toDomain(GraphMaterialStatsDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialStats(
                dataObject.getMaterialId(),
                dataObject.getDraftNodeCount(),
                dataObject.getDraftEdgeCount(),
                dataObject.getPublishedNodeCount(),
                dataObject.getPublishedEdgeCount(),
                dataObject.getActiveTaskCount(),
                dataObject.getPendingReviewTaskCount(),
                dataObject.getFailedTaskCount(),
                dataObject.getStatsRevision(),
                dataObject.getCalculatedAt());
    }
}
