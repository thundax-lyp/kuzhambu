package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.DomainException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionDecision;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphMaterialDeletionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphMaterialDeletionChangeId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphMaterialDeletionChange {
    public static final String LOCK_CONFLICT_CODE = "GRAPH_LOCK_CONFLICT";

    private GraphMaterialDeletionChangeId id;
    private Long materialId;
    private ContentRef materialRef;
    private String materialSnapshotJson;
    private GraphMaterialDeletionDecision decision;
    private GraphMaterialDeletionStatus status;
    private long lockVersion;
    private String resultSummaryJson;
    private Instant requestedAt;
    private Instant completedAt;

    public void requireLockVersion(long expectedLockVersion) {
        if (lockVersion != expectedLockVersion) {
            throw lockConflict();
        }
    }

    public void decide(GraphMaterialDeletionDecision decision, Instant decidedAt) {
        if (status != GraphMaterialDeletionStatus.AWAITING_DECISION) {
            throw new DomainException("Only awaiting graph material deletion changes can be decided");
        }
        this.decision = decision;
        this.status = GraphMaterialDeletionStatus.PENDING;
        this.completedAt = decidedAt;
    }

    public static DomainException lockConflict() {
        return new DomainException(LOCK_CONFLICT_CODE, "knowledge.graph.lock-conflict", "图谱对象已被其他操作修改，请刷新后重试");
    }
}
