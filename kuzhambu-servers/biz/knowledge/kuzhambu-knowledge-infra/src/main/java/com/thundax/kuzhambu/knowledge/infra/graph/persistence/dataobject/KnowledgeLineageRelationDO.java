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
@TableName("knowledge_lineage_relation")
public class KnowledgeLineageRelationDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String relationKey;
    private String sourceNodeKey;
    private String targetNodeKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Instant firstExtractedAt;
    private Instant lastExtractedAt;
    private Instant confirmedAt;
}
