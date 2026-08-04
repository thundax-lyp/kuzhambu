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
@TableName("knowledge_lineage_node")
public class KnowledgeLineageNodeDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nodeKey;
    private String name;
    private String nodeType;
    private Integer generation;
    private String gender;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Instant firstExtractedAt;
    private Instant lastExtractedAt;
    private Instant confirmedAt;
}
