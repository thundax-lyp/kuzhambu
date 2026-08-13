package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphEdgeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedEdgeId;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublishedEdge {
    private GraphPublishedEdgeId id;
    private GraphEdgeKey edgeKey;
    private GraphPublishedNodeId sourceNodeId;
    private GraphPublishedNodeId targetNodeId;
    private String relationType;
    private GraphSourceType source;
    private String qualifiersJson;
    private GraphPublishedStatus status;
    private Instant modifiedAt;
    private long lockVersion;

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw new DomainException("Graph published edge lock version mismatch");
        }
    }

    public void touch(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void delete(Instant modifiedAt) {
        if (status != GraphPublishedStatus.ACTIVE) {
            throw new DomainException("Only active graph published edges can be deleted");
        }
        status = GraphPublishedStatus.DELETED;
        touch(modifiedAt);
    }

    public void activate(Instant modifiedAt) {
        if (status != GraphPublishedStatus.DELETED) {
            throw new DomainException("Only deleted graph published edges can be activated");
        }
        status = GraphPublishedStatus.ACTIVE;
        touch(modifiedAt);
    }

    public void refreshEdgeKey(
            GraphNodeKey sourceNodeKey,
            GraphNodeKey targetNodeKey,
            boolean directed,
            Map<String, String> keyQualifiers) {
        validateRequiredFields();
        edgeKey = GraphKeyHelper.generateEdgeKey(sourceNodeKey, targetNodeKey, relationType, directed, keyQualifiers);
    }

    public void validateRequiredFields() {
        if (sourceNodeId == null || targetNodeId == null || isBlank(relationType) || source == null || status == null) {
            throw new DomainException("Graph published edge required fields are incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
