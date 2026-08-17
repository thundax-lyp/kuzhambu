package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionDisposition;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionExecutionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphExtractionTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphExtractionTask {
    private GraphExtractionTaskId id;
    private Long materialId;
    private ContentRef contentRef;
    private String contentSnapshotJson;
    private String modelSnapshotJson;
    private String promptSnapshotJson;
    private String outputSchemaJson;
    private GraphExtractionExecutionStatus executionStatus;
    private GraphExtractionDisposition disposition;
    private int attemptNo;
    private long lockVersion;
    private String batchId;
    private Long candidateId;
    private String currentStage;
    private int progress;
    private String idempotencyKey;
    private GraphExtractionTaskId regeneratedFromTaskId;
    private GraphExtractionTaskId supersededByTaskId;
    private GraphExtractionTaskId triggeredByTaskId;
    private Instant requestedAt;
    private Instant completedAt;
    private Instant disposedAt;
    private Instant purgeAfter;
}
