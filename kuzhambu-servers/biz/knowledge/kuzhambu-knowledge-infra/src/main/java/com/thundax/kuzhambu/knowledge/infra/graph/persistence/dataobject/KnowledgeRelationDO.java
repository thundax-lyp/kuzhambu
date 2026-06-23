package com.thundax.kuzhambu.knowledge.infra.graph.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_relation")
public class KnowledgeRelationDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long relationId;
    private String relationKey;
    private String sourceEntityKey;
    private String targetEntityKey;
    private String sourceName;
    private String targetName;
    private String relationType;
    private String evidence;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Date firstExtractedAt;
    private Date lastExtractedAt;
    private Date confirmedAt;
}
