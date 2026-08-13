package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_graph_published_node_property")
public class GraphPublishedNodePropertyDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publishedNodeId;
    private String propertyName;
    private String value;
    private Boolean preferred;
}
