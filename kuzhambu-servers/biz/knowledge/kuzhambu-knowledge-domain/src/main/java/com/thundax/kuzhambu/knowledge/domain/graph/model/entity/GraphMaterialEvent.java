package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialEventType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialEventId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialEvent {
    private GraphMaterialEventId id;
    private ContentRef materialRef;
    private GraphMaterialEventType type;
    private GraphMaterialEventStatus status;
    private Instant changedAt;
    private long lockVersion;

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw new DomainException("Graph material event lock version mismatch");
        }
    }

    public void startProcessing() {
        if (status != GraphMaterialEventStatus.SCHEDULED && status != GraphMaterialEventStatus.FAILED) {
            throw new DomainException("Only scheduled or failed graph material events can start processing");
        }
        status = GraphMaterialEventStatus.PROCESSING;
        changedAt = Instant.now();
    }

    public void succeed() {
        if (status != GraphMaterialEventStatus.PROCESSING) {
            throw new DomainException("Only processing graph material events can succeed");
        }
        status = GraphMaterialEventStatus.SUCCEEDED;
        changedAt = Instant.now();
    }

    public void fail() {
        if (status != GraphMaterialEventStatus.PROCESSING) {
            throw new DomainException("Only processing graph material events can fail");
        }
        status = GraphMaterialEventStatus.FAILED;
        changedAt = Instant.now();
    }

    public void scheduleRetry() {
        if (status != GraphMaterialEventStatus.FAILED) {
            throw new DomainException("Only failed graph material events can schedule retry");
        }
        status = GraphMaterialEventStatus.SCHEDULED;
        changedAt = Instant.now();
    }

    public void reclaimStaleProcessing() {
        if (status != GraphMaterialEventStatus.PROCESSING) {
            throw new DomainException("Only processing graph material events can be reclaimed");
        }
        status = GraphMaterialEventStatus.SCHEDULED;
        changedAt = Instant.now();
    }
}
