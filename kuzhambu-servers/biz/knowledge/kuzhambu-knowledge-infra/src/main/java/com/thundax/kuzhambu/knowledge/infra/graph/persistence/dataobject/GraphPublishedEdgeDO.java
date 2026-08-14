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
@TableName("knowledge_graph_published_edge")
public class GraphPublishedEdgeDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String edgeKey;
    private Long sourcePublishedNodeId;
    private Long targetPublishedNodeId;
    private String relationType;
    private String source;
    private String qualifiersJson;
    private String status;
    private Instant modifiedAt;
    private Long lockVersion;
}
