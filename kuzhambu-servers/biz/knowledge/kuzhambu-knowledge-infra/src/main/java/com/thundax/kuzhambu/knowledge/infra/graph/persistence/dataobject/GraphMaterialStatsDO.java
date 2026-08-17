package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_graph_material_stats")
public class GraphMaterialStatsDO {
    @TableId
    private Long materialId;

    private Long draftNodeCount;
    private Long draftEdgeCount;
    private Long publishedNodeCount;
    private Long publishedEdgeCount;
    private Long activeTaskCount;
    private Long pendingReviewTaskCount;
    private Long failedTaskCount;
    private Long statsRevision;
    private Instant calculatedAt;
}
