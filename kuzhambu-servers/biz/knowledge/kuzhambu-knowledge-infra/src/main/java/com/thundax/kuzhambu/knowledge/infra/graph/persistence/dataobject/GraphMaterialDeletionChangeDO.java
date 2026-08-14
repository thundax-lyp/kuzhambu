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
@TableName("knowledge_graph_material_deletion_change")
public class GraphMaterialDeletionChangeDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;
    private String contentType;
    private Long contentRefId;
    private String materialSnapshotJson;
    private String decision;
    private String status;
    private Long lockVersion;
    private String resultSummaryJson;
    private Instant requestedAt;
    private Instant completedAt;
}
