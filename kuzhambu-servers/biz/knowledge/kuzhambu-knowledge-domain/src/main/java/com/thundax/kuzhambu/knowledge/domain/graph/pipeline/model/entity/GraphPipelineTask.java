package com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.entity;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.knowledge.domain.graph.material.model.valueobject.GraphMaterialId;
import com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.enums.GraphPipelineTaskStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.pipeline.model.valueobject.GraphPipelineTaskId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphPipelineTask {
    private GraphPipelineTaskId id;
    private GraphMaterialId materialId;
    private ContentRef contentRef;
    private String contentSnapshotJson;
    private String pipelineVersion;
    private GraphPipelineTaskStatus status;
    private String currentStage;
    private int progress;
    private String resultSummaryJson;
    private String failureReason;
    private GraphPipelineTaskId retryFromTaskId;
    private Instant requestedAt;
    private Instant completedAt;

    public void startStage(String stageName, int stageProgress) {
        if (status != GraphPipelineTaskStatus.PENDING && status != GraphPipelineTaskStatus.RUNNING) {
            throw new IllegalStateException("Only pending or running pipeline tasks can start a stage");
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
