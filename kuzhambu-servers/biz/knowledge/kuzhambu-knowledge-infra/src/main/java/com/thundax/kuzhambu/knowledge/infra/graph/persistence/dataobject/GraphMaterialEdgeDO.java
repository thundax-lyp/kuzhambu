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
@TableName("knowledge_graph_material_edge")
public class GraphMaterialEdgeDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;
    private Long sourceMaterialNodeId;
    private Long targetMaterialNodeId;
    private String relationType;
    private String source;
    private String qualifiersJson;
    private String edgeKey;
}
