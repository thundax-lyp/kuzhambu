package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphGovernanceOperationId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphGovernanceOperation {

    private GraphGovernanceOperationId id;
    private String operationType;
    private String targetType;
    private Long targetId;
    private String beforeSnapshotJson;
    private String afterSnapshotJson;
    private String reason;
    private Long auditLogId;
    private Instant operatedAt;
}
