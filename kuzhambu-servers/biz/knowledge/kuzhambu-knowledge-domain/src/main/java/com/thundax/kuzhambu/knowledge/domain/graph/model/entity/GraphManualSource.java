package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphManualSourceId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphManualSource {

    private GraphManualSourceId id;
    private String targetType;
    private Long targetId;
    private String reason;
    private Long auditLogId;
    private Instant recordedAt;
}
