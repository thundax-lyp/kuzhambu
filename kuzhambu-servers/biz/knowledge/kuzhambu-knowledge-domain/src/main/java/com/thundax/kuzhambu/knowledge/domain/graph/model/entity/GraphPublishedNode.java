package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.helper.GraphKeyHelper;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphNodeType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPublishedStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphSourceType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphNodeKey;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphPublishedNodeId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublishedNode {
    private GraphPublishedNodeId id;
    private GraphNodeKey nodeKey;
    private GraphNodeType nodeType;
    private String name;
    private GraphSourceType source;
    private GraphPublishedStatus status;
    private Instant modifiedAt;
    private long lockVersion;

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw new DomainException("Graph published node lock version mismatch");
        }
    }

    public void touch(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void delete(Instant modifiedAt) {
        if (status != GraphPublishedStatus.ACTIVE) {
            throw new DomainException("Only active graph published nodes can be deleted");
        }
        status = GraphPublishedStatus.DELETED;
        touch(modifiedAt);
    }

    public void activate(Instant modifiedAt) {
        if (status != GraphPublishedStatus.DELETED) {
            throw new DomainException("Only deleted graph published nodes can be activated");
        }
        status = GraphPublishedStatus.ACTIVE;
        touch(modifiedAt);
    }

    public void refreshNodeKey(String identityQualifier) {
        validateRequiredFields();
        nodeKey = GraphKeyHelper.generateNodeKey(nodeType, name, identityQualifier);
    }

    public void validateRequiredFields() {
        if (nodeType == null || isBlank(name) || source == null || status == null) {
            throw new DomainException("Graph published node required fields are incomplete");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
