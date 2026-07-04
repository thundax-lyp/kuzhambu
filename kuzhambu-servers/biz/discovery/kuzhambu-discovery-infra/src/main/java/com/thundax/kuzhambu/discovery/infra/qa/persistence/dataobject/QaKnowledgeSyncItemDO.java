package com.thundax.kuzhambu.discovery.infra.qa.persistence.dataobject;

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
@TableName("discovery_qa_knowledge_sync_item")
public class QaKnowledgeSyncItemDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String sourceId;
    private String contentType;
    private Long contentId;
    private String knowledgeBaseName;
    private Integer currentVersionNo;
    private String knowledgeRevision;
    private String provider;
    private String externalKnowledgeBaseId;
    private String externalKnowledgeItemId;
    private String syncStatus;
    private String failureReason;
    private Date syncedAt;
    private Date createdAt;
    private Date updatedAt;
}
