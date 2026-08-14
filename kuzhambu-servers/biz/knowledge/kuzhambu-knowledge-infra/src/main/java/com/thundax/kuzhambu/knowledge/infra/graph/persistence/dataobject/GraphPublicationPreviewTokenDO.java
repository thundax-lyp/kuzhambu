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
@TableName("knowledge_graph_publication_preview_token")
public class GraphPublicationPreviewTokenDO {
    @TableId(type = IdType.INPUT)
    private String token;

    private Long materialId;
    private Long materialLockVersion;
    private String snapshotJson;
    private Instant expiresAt;
    private Instant consumedAt;
}
