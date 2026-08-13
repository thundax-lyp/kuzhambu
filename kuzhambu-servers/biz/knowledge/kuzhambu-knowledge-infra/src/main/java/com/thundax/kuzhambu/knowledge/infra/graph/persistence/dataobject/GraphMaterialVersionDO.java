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
@TableName("knowledge_graph_material_version")
public class GraphMaterialVersionDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;
    private Long versionNo;
    private String snapshotJson;
    private Long publishedBy;
    private Instant publishedAt;
}
