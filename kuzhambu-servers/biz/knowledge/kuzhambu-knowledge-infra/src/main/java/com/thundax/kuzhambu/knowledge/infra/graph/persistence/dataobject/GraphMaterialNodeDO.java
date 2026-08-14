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
@TableName("knowledge_graph_material_node")
public class GraphMaterialNodeDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;
    private String nodeKey;
    private String nodeType;
    private String name;
    private String source;
    private String propertiesJson;
}
