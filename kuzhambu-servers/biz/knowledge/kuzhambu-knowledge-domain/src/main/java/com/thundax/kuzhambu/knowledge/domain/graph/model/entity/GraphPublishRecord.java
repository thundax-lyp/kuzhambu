package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPublishRecord {
    private Long id;
    private ContentRef materialRef;
    private String status;
    private String previewSummaryJson;
    private String conflictDecisionsJson;
    private String resultSummaryJson;
    private Instant requestedAt;
    private Instant completedAt;
}
