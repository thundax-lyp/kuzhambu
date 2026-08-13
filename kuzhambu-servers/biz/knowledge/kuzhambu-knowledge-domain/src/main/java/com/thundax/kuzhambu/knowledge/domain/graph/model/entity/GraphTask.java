package com.thundax.kuzhambu.knowledge.domain.graph.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphPipelineTaskStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.valueobject.GraphTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphTask {
    private GraphTaskId id;
    private ContentRef contentRef;
    private String contentSnapshotJson;
    private String pipelineVersion;
    private GraphPipelineTaskStatus status;
    private String currentStage;
    private int progress;
    private String resultSummaryJson;
    private String failureReason;
    private GraphTaskId retryFromTaskId;
    private Instant requestedAt;
    private Instant completedAt;

    public void startStage(String stageName, int stageProgress) {
        if (status != GraphPipelineTaskStatus.PENDING && status != GraphPipelineTaskStatus.RUNNING) {
            throw new IllegalStateException("Only pending or running graph tasks can start a stage");
        }
        status = GraphPipelineTaskStatus.RUNNING;
        currentStage = stageName;
        progress = stageProgress;
    }

    public void succeed(String summaryJson, Instant finishedAt) {
        status = GraphPipelineTaskStatus.SUCCEEDED;
        progress = 100;
        resultSummaryJson = summaryJson;
        completedAt = finishedAt;
    }

    public void fail(String reason, Instant finishedAt) {
        status = GraphPipelineTaskStatus.FAILED;
        failureReason = reason;
        completedAt = finishedAt;
    }
}
