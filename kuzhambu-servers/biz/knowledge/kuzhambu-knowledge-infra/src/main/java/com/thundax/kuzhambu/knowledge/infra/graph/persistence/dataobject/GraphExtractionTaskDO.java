package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_graph_extraction_task")
public class GraphExtractionTaskDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;
    private String contentType;
    private Long contentRefId;
    private String contentSnapshotJson;
    private String pipelineVersion;
    private String modelSnapshotJson;
    private String promptSnapshotJson;
    private String outputSchemaJson;
    private String executionStatus;
    private String disposition;
    private Integer attemptNo;
    private Long lockVersion;
    private String batchId;
    private Long aiBatchId;
    private Long candidateId;
    private String currentStage;
    private Integer progress;
    private String failureReason;
    private String idempotencyScope;
    private String idempotencyKey;
    private Long requestedBy;
    private Long regeneratedFromTaskId;
    private Long supersededByTaskId;
    private Long triggeredByTaskId;
    private Instant requestedAt;
    private Instant completedAt;
    private Instant disposedAt;
    private Instant purgeAfter;
}
