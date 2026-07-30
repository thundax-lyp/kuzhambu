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
@TableName("knowledge_entity")
public class KnowledgeEntityDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String entityKey;
    private String name;
    private String entityType;
    private String description;
    private String confirmationStatus;
    private Long latestVersionId;
    private String sourceRefsJson;
    private Date firstExtractedAt;
    private Date lastExtractedAt;
    private Date confirmedAt;
}
