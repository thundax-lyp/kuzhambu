package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_graph_published_edge_material")
public class GraphPublishedEdgeMaterialDO {
    private Long publishedEdgeId;
    private String contentType;
    private Long contentRefId;
    private String sourceSnapshotJson;
    private Long changedAt;
}
