package com.thundax.kuzhambu.knowledge.infra.graph.persistence.assembler;

import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphEdgeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialDeletionChangeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialEventIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphMaterialVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphNodeKeyCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedEdgePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodeIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphPublishedNodePropertyIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialDeletionChange;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialEvent;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphMaterialVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdge;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedEdgeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeMaterial;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphPublishedNodeProperty;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialDeletionChangeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEdgeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialEventDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphMaterialVersionDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedEdgePropertyDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodeMaterialDO;
import com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject.GraphPublishedNodePropertyDO;

public final class GraphPersistenceAssembler {

    private GraphPersistenceAssembler() {}

    public static GraphMaterialDO toObject(GraphMaterial entity) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialDO(
                null,
                ContentRefCodec.toContentType(entity.getContentRef()),
                ContentRefCodec.toValue(entity.getContentRef()),
                entity.getContentTitleSnapshot(),
                entity.getStatus() == null ? null : entity.getStatus().value(),
                entity.getPublishedAt(),
                entity.getFailureReason(),
                entity.getFailedOperation(),
                entity.getLockVersion());
    }

    public static GraphMaterial toDomain(GraphMaterialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterial(
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                dataObject.getContentTitleSnapshot(),
                GraphMaterialStatus.from(dataObject.getStatus()),
                dataObject.getPublishedAt(),
                dataObject.getFailureReason(),
                dataObject.getFailedOperation(),
                dataObject.getLockVersion());
    }

    public static GraphMaterialNodeDO toObject(GraphMaterialNode entity, Long materialId) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialNodeDO(
                GraphMaterialNodeIdCodec.toValue(entity.getId()),
                materialId,
                GraphNodeKeyCodec.toValue(entity.getNodeKey()),
                entity.getNodeType() == null ? null : entity.getNodeType().value(),
                entity.getName(),
                entity.getSource() == null ? null : entity.getSource().value(),
                entity.getPropertiesJson());
    }

    public static GraphMaterialNode toDomain(GraphMaterialNodeDO dataObject, ContentRef materialRef) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialNode(
                GraphMaterialNodeIdCodec.toDomain(dataObject.getId()),
                materialRef,
                GraphNodeKeyCodec.toDomain(dataObject.getNodeKey()),
                GraphNodeType.from(dataObject.getNodeType()),
                dataObject.getName(),
                GraphSourceType.from(dataObject.getSource()),
                dataObject.getPropertiesJson());
    }

    public static GraphMaterialEdgeDO toObject(GraphMaterialEdge entity, Long materialId) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialEdgeDO(
                GraphMaterialEdgeIdCodec.toValue(entity.getId()),
                materialId,
                GraphMaterialNodeIdCodec.toValue(entity.getSourceNodeId()),
                GraphMaterialNodeIdCodec.toValue(entity.getTargetNodeId()),
                entity.getRelationType(),
                entity.getSource() == null ? null : entity.getSource().value(),
                entity.getQualifiersJson(),
                GraphEdgeKeyCodec.toValue(entity.getEdgeKey()));
    }

    public static GraphMaterialEdge toDomain(GraphMaterialEdgeDO dataObject, ContentRef materialRef) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialEdge(
                GraphMaterialEdgeIdCodec.toDomain(dataObject.getId()),
                materialRef,
                GraphMaterialNodeIdCodec.toDomain(dataObject.getSourceMaterialNodeId()),
                GraphMaterialNodeIdCodec.toDomain(dataObject.getTargetMaterialNodeId()),
                GraphEdgeKeyCodec.toDomain(dataObject.getEdgeKey()),
                dataObject.getRelationType(),
                GraphSourceType.from(dataObject.getSource()),
                dataObject.getQualifiersJson());
    }

    public static GraphMaterialVersionDO toObject(GraphMaterialVersion entity, Long materialId) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialVersionDO(
                GraphMaterialVersionIdCodec.toValue(entity.getId()),
                materialId,
                entity.getVersionNo(),
                entity.getSnapshotJson(),
                entity.getPublishedBy(),
                entity.getPublishedAt());
    }

    public static GraphMaterialVersion toDomain(GraphMaterialVersionDO dataObject, ContentRef materialRef) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialVersion(
                GraphMaterialVersionIdCodec.toDomain(dataObject.getId()),
                materialRef,
                dataObject.getVersionNo(),
                dataObject.getSnapshotJson(),
                dataObject.getPublishedBy(),
                dataObject.getPublishedAt());
    }

    public static GraphMaterialEventDO toObject(GraphMaterialEvent entity) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialEventDO(
                GraphMaterialEventIdCodec.toValue(entity.getId()),
                ContentRefCodec.toContentType(entity.getMaterialRef()),
                ContentRefCodec.toValue(entity.getMaterialRef()),
                entity.getType() == null ? null : entity.getType().value(),
                entity.getStatus() == null ? null : entity.getStatus().value(),
                entity.getChangedAt(),
                entity.getLockVersion());
    }

    public static GraphMaterialEvent toDomain(GraphMaterialEventDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialEvent(
                GraphMaterialEventIdCodec.toDomain(dataObject.getId()),
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                GraphMaterialEventType.from(dataObject.getEventType()),
                GraphMaterialEventStatus.from(dataObject.getStatus()),
                dataObject.getChangedAt(),
                dataObject.getLockVersion());
    }

    public static GraphMaterialDeletionChangeDO toObject(GraphMaterialDeletionChange entity) {
        if (entity == null) {
            return null;
        }
        return new GraphMaterialDeletionChangeDO(
                GraphMaterialDeletionChangeIdCodec.toValue(entity.getId()),
                entity.getMaterialId(),
                ContentRefCodec.toContentType(entity.getMaterialRef()),
                ContentRefCodec.toValue(entity.getMaterialRef()),
                entity.getMaterialSnapshotJson(),
                entity.getDecision() == null ? null : entity.getDecision().value(),
                entity.getStatus() == null ? null : entity.getStatus().value(),
                entity.getLockVersion(),
                entity.getResultSummaryJson(),
                entity.getRequestedAt(),
                entity.getCompletedAt());
    }

    public static GraphMaterialDeletionChange toDomain(GraphMaterialDeletionChangeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphMaterialDeletionChange(
                GraphMaterialDeletionChangeIdCodec.toDomain(dataObject.getId()),
                dataObject.getMaterialId(),
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                dataObject.getMaterialSnapshotJson(),
                GraphMaterialDeletionDecision.from(dataObject.getDecision()),
                GraphMaterialDeletionStatus.from(dataObject.getStatus()),
                dataObject.getLockVersion(),
                dataObject.getResultSummaryJson(),
                dataObject.getRequestedAt(),
                dataObject.getCompletedAt());
    }

    public static GraphPublishedNodeDO toObject(GraphPublishedNode entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedNodeDO(
                GraphPublishedNodeIdCodec.toValue(entity.getId()),
                GraphNodeKeyCodec.toValue(entity.getNodeKey()),
                entity.getNodeType() == null ? null : entity.getNodeType().value(),
                entity.getName(),
                entity.getSource() == null ? null : entity.getSource().value(),
                entity.getStatus() == null ? null : entity.getStatus().value(),
                entity.getModifiedAt(),
                entity.getLockVersion());
    }

    public static GraphPublishedNode toDomain(GraphPublishedNodeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedNode(
                GraphPublishedNodeIdCodec.toDomain(dataObject.getId()),
                GraphNodeKeyCodec.toDomain(dataObject.getNodeKey()),
                GraphNodeType.from(dataObject.getNodeType()),
                dataObject.getName(),
                GraphSourceType.from(dataObject.getSource()),
                GraphPublishedStatus.from(dataObject.getStatus()),
                dataObject.getModifiedAt(),
                dataObject.getLockVersion());
    }

    public static GraphPublishedEdgeDO toObject(GraphPublishedEdge entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedEdgeDO(
                GraphPublishedEdgeIdCodec.toValue(entity.getId()),
                GraphEdgeKeyCodec.toValue(entity.getEdgeKey()),
                GraphPublishedNodeIdCodec.toValue(entity.getSourceNodeId()),
                GraphPublishedNodeIdCodec.toValue(entity.getTargetNodeId()),
                entity.getRelationType(),
                entity.getSource() == null ? null : entity.getSource().value(),
                entity.getQualifiersJson(),
                entity.getStatus() == null ? null : entity.getStatus().value(),
                entity.getModifiedAt(),
                entity.getLockVersion());
    }

    public static GraphPublishedEdge toDomain(GraphPublishedEdgeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedEdge(
                GraphPublishedEdgeIdCodec.toDomain(dataObject.getId()),
                GraphEdgeKeyCodec.toDomain(dataObject.getEdgeKey()),
                GraphPublishedNodeIdCodec.toDomain(dataObject.getSourcePublishedNodeId()),
                GraphPublishedNodeIdCodec.toDomain(dataObject.getTargetPublishedNodeId()),
                dataObject.getRelationType(),
                GraphSourceType.from(dataObject.getSource()),
                dataObject.getQualifiersJson(),
                GraphPublishedStatus.from(dataObject.getStatus()),
                dataObject.getModifiedAt(),
                dataObject.getLockVersion());
    }

    public static GraphPublishedNodePropertyDO toObject(GraphPublishedNodeProperty entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedNodePropertyDO(
                GraphPublishedNodePropertyIdCodec.toValue(entity.getId()),
                GraphPublishedNodeIdCodec.toValue(entity.getPublishedNodeId()),
                entity.getPropertyKey(),
                entity.getValue(),
                entity.isPreferred());
    }

    public static GraphPublishedNodeProperty toDomain(GraphPublishedNodePropertyDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedNodeProperty(
                GraphPublishedNodePropertyIdCodec.toDomain(dataObject.getId()),
                GraphPublishedNodeIdCodec.toDomain(dataObject.getPublishedNodeId()),
                dataObject.getPropertyName(),
                dataObject.getValue(),
                Boolean.TRUE.equals(dataObject.getPreferred()));
    }

    public static GraphPublishedEdgePropertyDO toObject(GraphPublishedEdgeProperty entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedEdgePropertyDO(
                GraphPublishedEdgePropertyIdCodec.toValue(entity.getId()),
                GraphPublishedEdgeIdCodec.toValue(entity.getPublishedEdgeId()),
                entity.getPropertyKey(),
                entity.getValue(),
                entity.isPreferred());
    }

    public static GraphPublishedEdgeProperty toDomain(GraphPublishedEdgePropertyDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedEdgeProperty(
                GraphPublishedEdgePropertyIdCodec.toDomain(dataObject.getId()),
                GraphPublishedEdgeIdCodec.toDomain(dataObject.getPublishedEdgeId()),
                dataObject.getPropertyName(),
                dataObject.getValue(),
                Boolean.TRUE.equals(dataObject.getPreferred()));
    }

    public static GraphPublishedNodeMaterialDO toObject(GraphPublishedNodeMaterial entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedNodeMaterialDO(
                GraphPublishedNodeIdCodec.toValue(entity.getPublishedNodeId()),
                ContentRefCodec.toContentType(entity.getMaterialRef()),
                ContentRefCodec.toValue(entity.getMaterialRef()),
                entity.getSourceSnapshotJson());
    }

    public static GraphPublishedNodeMaterial toDomain(GraphPublishedNodeMaterialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedNodeMaterial(
                GraphPublishedNodeIdCodec.toDomain(dataObject.getPublishedNodeId()),
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                dataObject.getSourceSnapshotJson());
    }

    public static GraphPublishedEdgeMaterialDO toObject(GraphPublishedEdgeMaterial entity) {
        if (entity == null) {
            return null;
        }
        return new GraphPublishedEdgeMaterialDO(
                GraphPublishedEdgeIdCodec.toValue(entity.getPublishedEdgeId()),
                ContentRefCodec.toContentType(entity.getMaterialRef()),
                ContentRefCodec.toValue(entity.getMaterialRef()),
                entity.getSourceSnapshotJson());
    }

    public static GraphPublishedEdgeMaterial toDomain(GraphPublishedEdgeMaterialDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new GraphPublishedEdgeMaterial(
                GraphPublishedEdgeIdCodec.toDomain(dataObject.getPublishedEdgeId()),
                ContentRefCodec.toDomain(dataObject.getContentType(), dataObject.getContentRefId()),
                dataObject.getSourceSnapshotJson());
    }
}
