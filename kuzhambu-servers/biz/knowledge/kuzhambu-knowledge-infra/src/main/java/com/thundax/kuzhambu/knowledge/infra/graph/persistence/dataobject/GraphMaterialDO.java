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
@TableName("knowledge_graph_material")
public class GraphMaterialDO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String contentType;
    private Long contentRefId;
    private String contentTitleSnapshot;
    private String status;
    private Instant publishedAt;
    private String failureReason;
    private String failedOperation;
    private Long lockVersion;
}
