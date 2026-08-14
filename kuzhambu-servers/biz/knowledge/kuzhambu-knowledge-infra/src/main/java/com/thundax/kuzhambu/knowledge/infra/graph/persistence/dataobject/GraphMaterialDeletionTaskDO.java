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
@TableName("knowledge_graph_material_deletion_task")
public class GraphMaterialDeletionTaskDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deletionChangeId;
    private String idempotencyKey;
    private String status;
    private Long lockVersion;
    private Integer progress;
    private String failureReason;
    private String resultSummaryJson;
    private Instant requestedAt;
    private Instant completedAt;
}
